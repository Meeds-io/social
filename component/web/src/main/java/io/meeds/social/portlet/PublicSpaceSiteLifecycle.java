/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.portlet;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.portal.application.PortalApplication;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.ApplicationLifecycle;

public class PublicSpaceSiteLifecycle extends BaseComponentPlugin implements ApplicationLifecycle<PortalRequestContext> {

  private LayoutService         layoutService;

  private SpaceService          spaceService;

  private ResourceBundleService resourceBundleService;

  @Override
  public void onInit(Application app) throws Exception {
    layoutService = ExoContainerContext.getService(LayoutService.class);
    spaceService = ExoContainerContext.getService(SpaceService.class);
    resourceBundleService = ExoContainerContext.getService(ResourceBundleService.class);
  }

  public void onStartRequest(Application app, PortalRequestContext context) { // NOSONAR
    if (!(app instanceof PortalApplication)) {
      return;
    }
    SiteKey siteKey = context.getSiteKey();
    if (siteKey == null || siteKey.getType() != SiteType.PORTAL) {
      return;
    }
    PortalConfig portalConfig = layoutService.getPortalConfig(siteKey);
    if (portalConfig == null
        || StringUtils.isBlank(portalConfig.getProperty(SpaceUtils.PUBLIC_SITE_SPACE_ID))) {
      return;
    }
    String spaceId = portalConfig.getProperty(SpaceUtils.PUBLIC_SITE_SPACE_ID);
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      return;
    }
    SpaceUtils.setSpaceByContext(context, space);

    String browserTabTitle = resourceBundleService.getSharedString("UISpaceMenu.browser.publicSiteTitle", context.getLocale());
    context.getRequest().setAttribute(PortalRequestContext.REQUEST_TITLE, browserTabTitle.replace("{0}", space.getDisplayName()));
  }

}
