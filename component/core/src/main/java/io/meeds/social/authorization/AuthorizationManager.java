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

import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import lombok.Setter;

public class AuthorizationManager extends UserACL {

  @Setter
  private SpaceService  spaceService;

  @Setter
  private LayoutService layoutService;

  public AuthorizationManager(InitParams params) {
    super(params);
  }

  @Override
  public boolean hasEditPermission(Identity identity, String ownerType, String ownerId, String expression) {
    if (PortalConfig.GROUP_TEMPLATE.equalsIgnoreCase(ownerType)) {
      return isAdministrator(identity);
    } else if (isSpaceSite(ownerType, ownerId)) {
      Space space = getSpaceService().getSpaceByGroupId(ownerId);
      return space != null
             && identity != null
             && getSpaceService().canManageSpaceLayout(space, identity.getUserId());
    }
    return super.hasEditPermission(identity, ownerType, ownerId, expression)
           || isSpacesAdministrator(identity, ownerType, ownerId);
  }

  @Override
  public boolean hasAccessPermission(Identity identity, String ownerType, String ownerId, Stream<String> expressionsStream) {
    if (PortalConfig.GROUP_TEMPLATE.equalsIgnoreCase(ownerType)) {
      return isAdministrator(identity);
    } else {
      return super.hasAccessPermission(identity, ownerType, ownerId, expressionsStream)
             || isSpacesAdministrator(identity, ownerType, ownerId);
    }
  }

  private boolean isSpacesAdministrator(Identity identity, String ownerType, String ownerId) {
    if (isAdministrator(identity)) {
      return true;
    } else if (isSpaceSite(ownerType, ownerId)) {
      return getSpaceService().isSuperManager(getSpaceService().getSpaceByGroupId(ownerId), identity.getUserId());
    } else if (isSpacePublicSite(ownerType, ownerId)) {
      Space space = getSpaceService().getSpaceById(getSpaceIdFromPublicSite(ownerType, ownerId));
      return space != null
             && identity != null
             && getSpaceService().canManageSpacePublicSite(space, identity.getUserId());
    } else {
      return false;
    }
  }

  private boolean isSpaceSite(String ownerType, String ownerId) {
    return PortalConfig.GROUP_TYPE.equalsIgnoreCase(ownerType)
           && StringUtils.startsWith(ownerId, SpaceUtils.SPACE_GROUP_PREFIX);
  }

  private String getSpaceIdFromPublicSite(String ownerType, String ownerId) {
    PortalConfig portalConfig = getLayoutService().getPortalConfig(ownerType, ownerId);
    return portalConfig.getProperty(SpaceUtils.PUBLIC_SITE_SPACE_ID);
  }

  private boolean isSpacePublicSite(String ownerType, String ownerId) {
    PortalConfig portalConfig = getLayoutService().getPortalConfig(ownerType, ownerId);
    return portalConfig != null && portalConfig.getProperty(SpaceUtils.PUBLIC_SITE_SPACE_ID) != null;
  }

  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = ExoContainerContext.getService(SpaceService.class);
    }
    return spaceService;
  }

  private LayoutService getLayoutService() {
    if (layoutService == null) {
      layoutService = ExoContainerContext.getService(LayoutService.class);
    }
    return layoutService;
  }

}
