/*
 * This file is part of the Meeds project (https://meeds.io/).

 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.space.mock;

import org.exoplatform.application.registry.Application;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.service.LayoutService;
import org.exoplatform.portal.mop.service.NavigationService;
import org.exoplatform.portal.mop.storage.PageStorage;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceTemplateService;

public class MockSpaceApplicationHandler extends DefaultSpaceApplicationHandler {

  public MockSpaceApplicationHandler(InitParams params,
                                     LayoutService layoutService,
                                     NavigationService navigationService,
                                     PageStorage pageStorage,
                                     SpaceTemplateService spaceTemplateService) {
    super(params, layoutService, navigationService, pageStorage, spaceTemplateService);
  }

  @Override
  protected Page createSpacePage(UserPortalConfigService userPortalConfigService,
                                 Space space,
                                 Application app,
                                 org.exoplatform.portal.config.model.Application<Portlet> portletApplication,
                                 boolean isRoot) throws Exception {
    Page page = userPortalConfigService.createPageTemplate("space",
                                                           PortalConfig.GROUP_TYPE,
                                                           space.getGroupId());
    // setting some data to page.
    setPage(space, app, portletApplication, page);
    return page;
  }

}
