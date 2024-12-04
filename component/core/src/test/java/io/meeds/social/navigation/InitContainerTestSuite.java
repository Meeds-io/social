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
package io.meeds.social.navigation;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;

import io.meeds.social.navigation.listener.NavigationConfigurationSiteDisplayListenerTest;
import io.meeds.social.navigation.plugin.LinkSidebarPluginTest;
import io.meeds.social.navigation.plugin.PageSidebarPluginTest;
import io.meeds.social.navigation.plugin.SiteSidebarPluginTest;
import io.meeds.social.navigation.plugin.SpaceListSidebarPluginTest;
import io.meeds.social.navigation.plugin.SpaceTemplateSidebarPluginTest;
import io.meeds.social.navigation.service.NavigationConfigurationServiceTest;

@RunWith(Suite.class)
@SuiteClasses({
  LinkSidebarPluginTest.class,
  PageSidebarPluginTest.class,
  SiteSidebarPluginTest.class,
  SpaceListSidebarPluginTest.class,
  SpaceTemplateSidebarPluginTest.class,
  NavigationConfigurationServiceTest.class,
  NavigationConfigurationSiteDisplayListenerTest.class,
})
public class InitContainerTestSuite {

  @BeforeClass
  public static void setUp() {
    if (PortalContainer.getInstanceIfPresent() != null) {
      PortalContainer.getInstance().stop();
      ExoContainerContext.setCurrentContainer(null);
    }
  }

}
