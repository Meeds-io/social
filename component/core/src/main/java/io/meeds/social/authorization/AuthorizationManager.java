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
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.space.template.model.SpaceTemplate;
import io.meeds.social.space.template.service.SpaceTemplateService;

import lombok.Setter;

public class AuthorizationManager extends UserACL {

  @Setter
  private SpaceService                spaceService;

  @Setter
  private SpaceTemplateService        spaceTemplateService;

  public AuthorizationManager(InitParams params) {
    super(params);
  }

  @Override
  public boolean hasEditPermission(Identity identity, String ownerType, String ownerId, String expression) {
    if (PortalConfig.GROUP_TEMPLATE.equalsIgnoreCase(ownerType)) {
      SpaceTemplate spaceTemplate = getSpaceTemplateService().getSpaceTemplateByLayout(ownerId);
      return spaceTemplate == null ? isAdministrator(identity) : !spaceTemplate.isSystem() && isSpacesAdministrator(identity);
    } else if (isSpaceSite(ownerType, ownerId)) {
      Space space = getSpaceService().getSpaceByGroupId(ownerId);
      return space != null
             && identity != null
             && getSpaceService().canManageSpaceLayout(space, identity.getUserId());
    }
    return isSpacesAdministrator(identity, ownerType, ownerId)
           || super.hasEditPermission(identity, ownerType, ownerId, expression);
  }

  @Override
  public boolean hasAccessPermission(Identity identity, String ownerType, String ownerId, Stream<String> expressionsStream) {
    if (PortalConfig.GROUP_TEMPLATE.equalsIgnoreCase(ownerType)) {
      SpaceTemplate spaceTemplate = getSpaceTemplateService().getSpaceTemplateByLayout(ownerId);
      return spaceTemplate == null ? isAdministrator(identity) : isSpacesAdministrator(identity);
    }
    return isSpacesAdministrator(identity, ownerType, ownerId)
           || super.hasAccessPermission(identity, ownerType, ownerId, expressionsStream);
  }

  private boolean isSpacesAdministrator(Identity identity, String ownerType, String ownerId) {
    return isSpaceSite(ownerType, ownerId) && isSpacesAdministrator(identity);
  }

  private boolean isSpacesAdministrator(Identity identity) {
    if (isAdministrator(identity)) {
      return true;
    } else {
      return getSpaceService().isSuperManager(identity.getUserId());
    }
  }

  private boolean isSpaceSite(String ownerType, String ownerId) {
    return PortalConfig.GROUP_TYPE.equalsIgnoreCase(ownerType)
           && StringUtils.startsWith(ownerId, SpaceUtils.SPACE_GROUP_PREFIX);
  }

  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = ExoContainerContext.getService(SpaceService.class);
    }
    return spaceService;
  }

  public SpaceTemplateService getSpaceTemplateService() {
    if (spaceTemplateService == null) {
      spaceTemplateService = ExoContainerContext.getService(SpaceTemplateService.class);
    }
    return spaceTemplateService;
  }
}
