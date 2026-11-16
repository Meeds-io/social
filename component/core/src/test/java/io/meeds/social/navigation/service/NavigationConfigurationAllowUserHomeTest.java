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
package io.meeds.social.navigation.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import org.exoplatform.services.listener.ListenerService;

import io.meeds.portal.navigation.constant.SidebarMode;
import io.meeds.portal.navigation.model.NavigationConfiguration;
import io.meeds.portal.navigation.model.SidebarConfiguration;
import io.meeds.portal.navigation.model.TopbarConfiguration;
import io.meeds.social.navigation.storage.NavigationConfigurationStorage;

public class NavigationConfigurationAllowUserHomeTest {

  private final Map<String, SettingValue<?>> store = new HashMap<>();

  private SettingService                     settingService;

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
  }

  @Test
  public void allowUserHomeChangedOnOneNodeIsSeenByTheOtherNode() {
    NavigationConfigurationServiceImpl nodeA = newNode();
    NavigationConfigurationServiceImpl nodeB = newNode();
    assertTrue(nodeB.isAllowUserHome());

    nodeA.updateConfiguration(configuration(false));
    assertFalse(nodeB.isAllowUserHome());
    assertFalse(nodeA.isAllowUserHome());

    nodeA.updateConfiguration(configuration(true));
    assertTrue(nodeB.isAllowUserHome());
  }

  @Test
  public void allowUserHomeIsReadWithoutCopyingTheConfiguration() {
    NavigationConfigurationStorage storage = mock(NavigationConfigurationStorage.class);
    when(storage.isAllowUserHome(any())).thenReturn(false);
    NavigationConfigurationServiceImpl service = newNode();
    ReflectionTestUtils.setField(service, "navigationConfigurationStorage", storage);

    assertFalse(service.isAllowUserHome());

    verify(storage).isAllowUserHome(any());
    verify(storage, never()).getConfiguration(any());
  }

  private NavigationConfigurationServiceImpl newNode() {
    NavigationConfigurationStorage storage = new NavigationConfigurationStorage();
    ReflectionTestUtils.setField(storage, "settingService", settingService);
    NavigationConfigurationServiceImpl service = new NavigationConfigurationServiceImpl();
    ReflectionTestUtils.setField(service, "navigationConfigurationStorage", storage);
    ReflectionTestUtils.setField(service, "listenerService", mock(ListenerService.class));
    service.setDefaultTopbarApplications(new ArrayList<>());
    return service;
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
