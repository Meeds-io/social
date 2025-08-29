/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.listeners;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import lombok.SneakyThrows;

public class SpaceInvitationUserEventListener extends UserEventListener {

  private static final Log    LOG = ExoLogger.getLogger(SpaceInvitationUserEventListener.class);

  private ExoContainer        container;

  private SpaceService        spaceService;

  private OrganizationService organizationService;

  public SpaceInvitationUserEventListener(ExoContainer container) {
    this.container = container;
  }

  @Override
  public void postSave(User user, boolean isNew) throws Exception {
    if (!isNew) {
      return;
    }
    List<String> spacesToJoin = getSpaceService().findExternalInvitationsSpacesByEmail(user.getEmail());
    if (CollectionUtils.isNotEmpty(spacesToJoin)) {
      for (String spaceId : spacesToJoin) {
        addUserToSpace(user, spaceId, 5);
      }
      spaceService.deleteExternalUserInvitations(user.getEmail());
    }
    RequestLifeCycle.restartTransaction();
  }

  @SneakyThrows
  public void addUserToSpace(User user, String spaceId, int tentative) {
    String username = user.getUserName();
    Space space = spaceService.getSpaceById(spaceId);
    spaceService.addMember(space, username);
    Collection<Membership> memberships = getOrganizationService().getMembershipHandler().findMembershipsByUser(username);
    if (memberships.stream().noneMatch(m -> m.getGroupId().equals(space.getGroupId()))) {
      if (tentative > 1) {
        LOG.warn("User invitation '{}' to space '{}({})' seems not considered, attempt adding user to space again",
                 username,
                 space.getDisplayName(),
                 space.getId());
        spaceService.removeMember(space, username);
        RequestLifeCycle.restartTransaction();
        addUserToSpace(user, spaceId, tentative - 1);
      } else {
        LOG.warn("User invitation '{}' to space '{}({})' seems not considered. Ignore adding User to space.",
                 username,
                 space.getDisplayName(),
                 space.getId());
      }
    } else {
      LOG.info("Invited user '{}' to space '{}({})' added as member",
               username,
               space.getDisplayName(),
               space.getId());
    }
  }

  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = container.getComponentInstanceOfType(SpaceService.class);
    }
    return spaceService;
  }

  public OrganizationService getOrganizationService() {
    if (organizationService == null) {
      organizationService = container.getComponentInstanceOfType(OrganizationService.class);
    }
    return organizationService;
  }
}
