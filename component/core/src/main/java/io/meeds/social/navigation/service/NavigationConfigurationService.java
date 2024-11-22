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

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.model.NavigationConfiguration;
import io.meeds.social.navigation.model.SidebarItem;
import io.meeds.social.navigation.model.TopbarApplication;
import io.meeds.social.navigation.plugin.DefaultSidebarPlugin;
import io.meeds.social.navigation.plugin.SidebarPlugin;
import io.meeds.social.navigation.storage.NavigationConfigurationStorage;

import lombok.Setter;
import lombok.SneakyThrows;

/**
 * A Service to manage Topbar and Sidebar configurations
 */
@Service
public class NavigationConfigurationService {

  private static final SidebarPlugin     DEFAULT_MENU_PLUGIN = new DefaultSidebarPlugin();

  @Autowired
  private NavigationConfigurationStorage navigationConfigurationStorage;

  @Autowired
  private List<SidebarPlugin>            menuPlugins;

  @Setter
  private List<TopbarApplication>        defaultTopbarApplications;

  /**
   * @return {@link NavigationConfiguration} with the complete configuration of
   *         Navigation
   */
  public NavigationConfiguration getConfiguration() {
    return getConfiguration(null, null, false);
  }

  /**
   * @param resolve either resolve name and icon of elements or not
   * @return {@link NavigationConfiguration} with the complete configuration of
   *         Navigation
   */
  public NavigationConfiguration getConfiguration(String username, Locale locale, boolean resolve) {
    NavigationConfiguration configuration = navigationConfigurationStorage.getConfiguration(defaultTopbarApplications);
    if (configuration == null) {
      return null;
    } else {
      configuration.getSidebar()
                   .setItems(configuration.getSidebar()
                                          .getItems()
                                          .stream()
                                          .filter(item -> getPlugin(item.getType()).itemExists(item, username))
                                          .map(item -> resolve ? expandSidebarItem(item, username, locale) : item)
                                          .toList());
      return configuration;
    }
  }

  /**
   * Updates the Navigation configuration
   * 
   * @param navigationConfiguration
   */
  public void updateConfiguration(NavigationConfiguration navigationConfiguration) {
    navigationConfigurationStorage.updateConfiguration(navigationConfiguration);
  }

  @SneakyThrows
  private SidebarItem expandSidebarItem(SidebarItem item, String username, Locale locale) {
    return getPlugin(item.getType()).resolveProperties(item, username, locale);
  }

  private SidebarPlugin getPlugin(SidebarItemType type) {
    return menuPlugins == null ? DEFAULT_MENU_PLUGIN :
                               menuPlugins.stream()
                                          .filter(p -> p.getType() == type)
                                          .findFirst()
                                          .orElse(DEFAULT_MENU_PLUGIN);
  }

}
