/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
package io.meeds.social.authorization;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.SpacesAdministrationService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import lombok.Setter;

public class AuthorizationManager extends UserACL {

  @Setter
  private SpacesAdministrationService spacesAdministrationService;

  @Setter
  private SpaceService                spaceService;

  public AuthorizationManager(InitParams params) {
    super(params);
  }

  @Override
  public boolean hasEditPermission(Identity identity, String ownerType, String ownerId, String expression) {
    if (isSpaceOwnerId(ownerType, ownerId)) {
      Space space = getSpaceService().getSpaceByGroupId(ownerId);
      if (space != null && identity != null) {
        if (CollectionUtils.isNotEmpty(space.getLayoutPermissions())) {
          return isAdministrator(identity)
                 || isSpacesAdministrator(identity)
                 || space.getLayoutPermissions()
                         .stream()
                         .anyMatch(permission -> identity.isMemberOf(getMembershipEntry(permission)));
        } else {
          return isAdministrator(identity) || isSpacesAdministrator(identity);
        }
      }
    }
    return isAdministrator(identity, ownerType, ownerId)
           || super.hasEditPermission(identity, ownerType, ownerId, expression);
  }

  @Override
  public boolean hasAccessPermission(Identity identity, String ownerType, String ownerId, Stream<String> expressionsStream) {
    return isAdministrator(identity, ownerType, ownerId)
           || super.hasAccessPermission(identity, ownerType, ownerId, expressionsStream);
  }

  private boolean isAdministrator(Identity identity, String ownerType, String ownerId) {
    return isSpaceOwnerId(ownerType, ownerId) && isSpacesAdministrator(identity);
  }

  private boolean isSpacesAdministrator(Identity identity) {
    List<MembershipEntry> spacesAdministrators = getSpacesAdministrationService().getSpacesAdministratorsMemberships();
    return CollectionUtils.isNotEmpty(spacesAdministrators)
           && spacesAdministrators.stream()
                                  .anyMatch(permission -> isMemberOf(identity, permission.toString()));
  }

  private boolean isSpaceOwnerId(String ownerType, String ownerId) {
    return PortalConfig.GROUP_TYPE.equalsIgnoreCase(ownerType)
           && StringUtils.startsWith(ownerId, SpaceUtils.SPACE_GROUP_PREFIX);
  }

  private MembershipEntry getMembershipEntry(String expression) {
    return expression.contains(":") ? MembershipEntry.parse(expression) : new MembershipEntry(expression);
  }

  private SpacesAdministrationService getSpacesAdministrationService() {
    if (spacesAdministrationService == null) {
      spacesAdministrationService = ExoContainerContext.getService(SpacesAdministrationService.class);
    }
    return spacesAdministrationService;
  }

  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = ExoContainerContext.getService(SpaceService.class);
    }
    return spaceService;
  }
}
