/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.navigation.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

import io.meeds.portal.navigation.constant.SidebarMode;
import io.meeds.portal.navigation.model.NavigationConfiguration;
import io.meeds.portal.navigation.model.SidebarConfiguration;
import io.meeds.portal.navigation.model.TopbarConfiguration;

public class NavigationConfigurationStorageTest {

  private final Map<String, SettingValue<?>> store = new HashMap<>();

  private SettingService                     settingService;

  private NavigationConfigurationStorage     nodeA;

  private NavigationConfigurationStorage     nodeB;

  @Before
  public void setUp() {
    settingService = mock(SettingService.class);
    when(settingService.get(any(Context.class), any(Scope.class), any(String.class))).thenAnswer(invocation -> store.get(invocation.getArgument(0).toString()
        + invocation.getArgument(2)));
    doAnswer(invocation -> store.put(invocation.getArgument(0).toString() + invocation.getArgument(2),
                                     invocation.getArgument(3))).when(settingService)
                                                                .set(any(Context.class),
                                                                     any(Scope.class),
                                                                     any(String.class),
                                                                     any(SettingValue.class));
    nodeA = newNode();
    nodeB = newNode();
  }

  @Test
  public void configurationUpdatedOnOneNodeIsSeenByTheOtherNode() {
    assertNull(nodeB.getConfiguration(null));
    nodeA.updateConfiguration(configuration(true));
    assertTrue(nodeB.getConfiguration(null).getSidebar().isAllowUserCustomHome());

    nodeA.updateConfiguration(configuration(false));
    assertFalse(nodeB.getConfiguration(null).getSidebar().isAllowUserCustomHome());
    assertFalse(nodeA.getConfiguration(null).getSidebar().isAllowUserCustomHome());
  }

  @Test
  public void unchangedStoredValueIsReturnedAsIndependentCopies() {
    nodeA.updateConfiguration(configuration(true));
    NavigationConfiguration first = nodeB.getConfiguration(null);
    first.getSidebar().setAllowUserCustomHome(false);
    assertEquals(true, nodeB.getConfiguration(null).getSidebar().isAllowUserCustomHome());
  }

  @Test
  public void storedValueParsedToNullFallsBackToDefaults() {
    settingService.set(Context.GLOBAL.id("NavigationConfiguration"),
                       Scope.APPLICATION.id("NavigationConfiguration"),
                       "configuration",
                       SettingValue.create("null"));

    assertNull(nodeA.getConfiguration(null));
    assertTrue(nodeA.isAllowUserHome(null));
  }

  @Test
  public void allowUserHomeIsReadFromTheStoredConfigurationOfAnyNode() {
    assertTrue(nodeB.isAllowUserHome(null));
    nodeA.updateConfiguration(configuration(false));
    assertFalse(nodeB.isAllowUserHome(null));
  }

  private NavigationConfigurationStorage newNode() {
    NavigationConfigurationStorage storage = new NavigationConfigurationStorage();
    ReflectionTestUtils.setField(storage, "settingService", settingService);
    return storage;
  }

  private NavigationConfiguration configuration(boolean allowUserCustomHome) {
    TopbarConfiguration topbar = new TopbarConfiguration();
    topbar.setApplications(new ArrayList<>());
    SidebarConfiguration sidebar = new SidebarConfiguration();
    sidebar.setAllowUserCustomHome(allowUserCustomHome);
    sidebar.setDefaultMode(SidebarMode.ICON);
    sidebar.setAllowedModes(List.of(SidebarMode.ICON));
    sidebar.setItems(new ArrayList<>());
    return new NavigationConfiguration(topbar, sidebar);
  }

}
