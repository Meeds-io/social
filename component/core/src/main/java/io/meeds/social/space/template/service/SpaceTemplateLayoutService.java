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
package io.meeds.social.space.template.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;

import lombok.SneakyThrows;

@Service
public class SpaceTemplateLayoutService {

  public static final String      DEFAULT_PUBLIC_SITE_TEMPLATE = "spacePublic";

  private UserPortalConfigService portalConfigService;

  private LayoutService           layoutService;

  public SpaceTemplateLayoutService(UserPortalConfigService portalConfigService, LayoutService layoutService) {
    this.portalConfigService = portalConfigService;
    this.layoutService = layoutService;
  }

  @SneakyThrows
  public long createSpacePublicSite(Space space,
                                    String name,
                                    String label,
                                    String[] accessPermissions) {
    String siteName = StringUtils.firstNonBlank(name, space.getPrettyName());
    portalConfigService.createUserPortalConfig(PortalConfig.PORTAL_TYPE, siteName, DEFAULT_PUBLIC_SITE_TEMPLATE);
    PortalConfig portalConfig = layoutService.getPortalConfig(siteName);
    if (accessPermissions != null) {
      portalConfig.setAccessPermissions(accessPermissions);
      portalConfig.setEditPermission(SpaceUtils.MANAGER + ":" + space.getGroupId());
    }
    portalConfig.setLabel(StringUtils.firstNonBlank(label, space.getDisplayName()));
    portalConfig.setDefaultSite(false);
    portalConfig.setProperty(SpaceUtils.PUBLIC_SITE_SPACE_ID, space.getId());
    portalConfig.setProperty(SpaceUtils.IS_PUBLIC_SITE_SPACE, "true");
    layoutService.save(portalConfig);
    return Long.parseLong(portalConfig.getStorageId().split("_")[1]);
  }

}
