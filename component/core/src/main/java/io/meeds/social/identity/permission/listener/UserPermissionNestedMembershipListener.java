/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.identity.permission.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.NestedMembership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.IdentityStorage;

import io.meeds.social.identity.permission.service.UserPermissionService;

import jakarta.annotation.PostConstruct;

/**
 * Handles the nested-group link/unlink
 * affects every member of the child group. Runs synchronously, on the same
 * thread/transaction as the triggering {@code linkGroups}/{@code unlinkGroups}
 * call (deliberately NOT {@code @Asynchronous}): the group/membership graph is
 * read via the same organization-service session as the not-yet-flushed write,
 * which is the only way to observe it before commit - an async hop would run on
 * a different thread/connection and could read a stale, pre-link/pre-unlink
 * view of the graph.
 */
@Component
public class UserPermissionNestedMembershipListener extends Listener<NestedMembership, NestedMembership> {

  private static final Log      LOG        = ExoLogger.getLogger(UserPermissionNestedMembershipListener.class);

  private static final int      BATCH_SIZE = 50;

  @Autowired
  private ListenerService       listenerService;

  @Autowired
  private OrganizationService   organizationService;

  @Autowired
  private IdentityManager       identityManager;

  @Autowired
  private UserPermissionService userPermissionService;

  @Autowired
  private IndexingService       indexingService;

  @Autowired
  private IdentityStorage       identityStorage;

  @PostConstruct
  public void init() {
    listenerService.addListener(UserPermissionGroupListener.NESTED_MEMBERSHIP_CHANGED_EVENT, this);
  }

  @Override
  public void onEvent(Event<NestedMembership, NestedMembership> event) throws Exception {
    NestedMembership nestedMembership = event.getData();
    String nestedGroupId = nestedMembership.getNestedGroupId();
    Group nestedGroup = organizationService.getGroupHandler().findGroupById(nestedGroupId);
    if (nestedGroup == null) {
      return;
    }
    ListAccess<Membership> membershipsListAccess = organizationService.getMembershipHandler()
                                                                      .findAllMembershipsByGroup(nestedGroup);
    int size = membershipsListAccess.getSize();
    for (int offset = 0; offset < size; offset += BATCH_SIZE) {
      int limit = Math.min(BATCH_SIZE, size - offset);
      Membership[] memberships = membershipsListAccess.load(offset, limit);
      for (Membership membership : memberships) {
        if (membership != null) {
          recomputeForUser(membership.getUserName());
        }
      }
    }
    if (size > 0) {
      // One invalidation for the whole batch rather than per-user, since it is a full
      // cache clear
      // (see UserPermissionMembershipListener).
      identityStorage.updateIdentityMembership(null);
    }
  }

  private void recomputeForUser(String userName) {
    try {
      Identity identity = identityManager.getOrCreateUserIdentity(userName);
      if (identity == null) {
        return;
      }
      long identityId = Long.parseLong(identity.getId());
      userPermissionService.recomputeInheritedMemberships(identityId,
                                                          userName,
                                                          organizationService.getMembershipHandler()
                                                                             .findMembershipsByUser(userName, true));
      indexingService.reindex(UserPermissionService.INDEX_CONNECTOR_NAME, userName);
      indexingService.reindex(ProfileIndexingServiceConnector.TYPE, String.valueOf(identityId));
    } catch (Exception e) {
      LOG.warn("Error recomputing inherited user permissions for user {}", userName, e);
    }
  }

}
