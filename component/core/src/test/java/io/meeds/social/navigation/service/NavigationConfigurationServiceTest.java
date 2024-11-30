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
package io.meeds.social.navigation.service;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.navigation.AbstractNavigationConfigurationTest;
import io.meeds.social.navigation.constant.SidebarMode;
import io.meeds.social.navigation.model.NavigationConfiguration;
import io.meeds.social.navigation.model.SidebarConfiguration;
import io.meeds.social.navigation.model.SidebarItem;
import io.meeds.social.navigation.model.TopbarConfiguration;

public class NavigationConfigurationServiceTest extends AbstractNavigationConfigurationTest {

  @Before
  @Override
  public void beforeEach() throws Exception {
    super.beforeEach();
    if (spaceTemplate == null) {
      mockSpaceTemplate();
      for (int i = 0; i < 5; i++) {
        Space space = new Space();
        space.setRegistration(Space.OPEN);
        space.setVisibility(Space.PUBLIC);
        space.setTemplateId(spaceTemplate.getId());
        spaceService.createSpace(space, userAcl.getSuperUser());
      }
    }
  }

  @Test
  public void testGetConfiguration() {
    NavigationConfiguration configuration = navigationConfigurationService.getConfiguration();
    assertNotNull(configuration);
    assertNotNull(configuration.getTopbar());
    assertNotNull(configuration.getSidebar());
  }

  @Test
  public void testGetConfigurationByUser() {
    NavigationConfiguration configuration = navigationConfigurationService.getConfiguration(userAcl.getSuperUser(),
                                                                                            Locale.FRENCH,
                                                                                            true);
    assertNotNull(configuration);

    TopbarConfiguration topbar = configuration.getTopbar();
    assertNotNull(topbar);
    assertTrue(topbar.isDisplayCompanyName());
    assertTrue(topbar.isDisplaySiteName());

    SidebarConfiguration sidebar = configuration.getSidebar();
    assertNotNull(sidebar);
    assertTrue(sidebar.isAllowUserCustomHome());
    assertEquals(3, sidebar.getAllowedModes().size());
    assertEquals(SidebarMode.ICON, sidebar.getDefaultMode());
    assertNotNull(sidebar.getItems());
    assertFalse(sidebar.getItems().isEmpty());
    SidebarItem item = sidebar.getItems().getFirst();
    assertEquals("contribute", item.getName());
  }

  @Test
  public void testSidebarUserMode() {
    NavigationConfiguration configuration = navigationConfigurationService.getConfiguration(userAcl.getSuperUser(),
                                                                                            Locale.FRENCH,
                                                                                            true);

    SidebarMode sidebarUserMode = navigationConfigurationService.getSidebarUserMode(userAcl.getSuperUser());
    assertEquals(configuration.getSidebar().getDefaultMode(), sidebarUserMode);

    navigationConfigurationService.updateSidebarUserMode(userAcl.getSuperUser(), SidebarMode.STICKY);
    sidebarUserMode = navigationConfigurationService.getSidebarUserMode(userAcl.getSuperUser());
    assertEquals(SidebarMode.STICKY, sidebarUserMode);

    navigationConfigurationService.updateSidebarUserMode(userAcl.getSuperUser(), SidebarMode.ICON);
    sidebarUserMode = navigationConfigurationService.getSidebarUserMode(userAcl.getSuperUser());
    assertEquals(SidebarMode.ICON, sidebarUserMode);
  }

}
