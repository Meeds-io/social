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

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.addons.AddOnService;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.services.listener.ListenerService;

import io.meeds.common.ContainerTransactional;
import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.constant.SidebarMode;
import io.meeds.social.navigation.constant.TopbarItemType;
import io.meeds.social.navigation.model.NavigationConfiguration;
import io.meeds.social.navigation.model.SidebarConfiguration;
import io.meeds.social.navigation.model.SidebarItem;
import io.meeds.social.navigation.model.TopbarApplication;
import io.meeds.social.navigation.model.TopbarConfiguration;
import io.meeds.social.navigation.plugin.DefaultSidebarPlugin;
import io.meeds.social.navigation.plugin.SidebarPlugin;
import io.meeds.social.navigation.storage.NavigationConfigurationStorage;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * A Service to manage Topbar and Sidebar configurations
 */
@Service
public class NavigationConfigurationService {

  public static final String             NAVIGATION_CONFIGURATION_UPDATED_EVENT = "social.navigation.configuration.updated";

  private static final String            TOPBAR_APPLICATION_ENABLED_PATTERN     = "social.topbar.application.%s.enabled";

  private static final String            TOPBAR_APPLICATION_MOBILE_PATTERN      = "social.topbar.application.%s.mobile";

  private static final String            TOP_NAVIGATION_ADDON_CONTAINER         = "middle-topNavigation-container";

  private static final SidebarPlugin     DEFAULT_MENU_PLUGIN                    = new DefaultSidebarPlugin();

  @Autowired
  private NavigationConfigurationStorage navigationConfigurationStorage;

  @Autowired
  private UserPortalConfigService        userPortalConfigService;

  @Autowired
  private AddOnService                   addonContainerService;

  @Autowired
  private Environment                    environment;

  @Autowired
  private ListenerService                listenerService;

  @Autowired
  private List<SidebarPlugin>            menuPlugins;

  @Setter
  private List<TopbarApplication>        defaultTopbarApplications;

  @PostConstruct
  @ContainerTransactional
  public void init() {
    this.defaultTopbarApplications = getDefaultTopbarApplications();
    NavigationConfiguration configuration = getConfiguration();
    if (configuration != null) {
      userPortalConfigService.setAllowUserHome(configuration.getSidebar().isAllowUserCustomHome());
    }
  }

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
                                          .filter(item -> !resolve || getPlugin(item.getType()).itemExists(item, username))
                                          .map(item -> resolve ? expandSidebarItem(item, username, locale) : item)
                                          .toList());
      return configuration;
    }
  }

  /**
   * @param username User name
   * @param locale {@link Locale} to compute Menu item names
   * @return {@link TopbarConfiguration} switch user role and customized
   *         settings
   */
  public TopbarConfiguration getTopbarConfiguration(String username, Locale locale) {
    return getConfiguration(username, locale, true).getTopbar();
  }

  /**
   * @param username User name
   * @param locale {@link Locale} to compute Menu item names
   * @return {@link SidebarConfiguration} switch user role and customized
   *         settings
   */
  public SidebarConfiguration getSidebarConfiguration(String username, Locale locale) {
    SidebarConfiguration sidebarConfiguration = getConfiguration(username, locale, true).getSidebar();
    if (StringUtils.isNotBlank(username)) {
      SidebarMode mode = getSidebarUserMode(username, sidebarConfiguration);
      sidebarConfiguration.setUserMode(mode);
    }
    return sidebarConfiguration;
  }

  /**
   * Retrieves the preferred mode of sidebar by a user
   * 
   * @param username User name as identifier
   * @return preferred {@link SidebarMode} or default if not set by user yet
   */
  public SidebarMode getSidebarUserMode(String username) {
    NavigationConfiguration configuration = navigationConfigurationStorage.getConfiguration(defaultTopbarApplications);
    return getSidebarUserMode(username, configuration.getSidebar());
  }

  /**
   * Updates the preferred mode of sidebar by the user
   * 
   * @param username User name as identifier
   * @param mode Preferred {@link SidebarMode} by the user
   */
  public void updateSidebarUserMode(String username, SidebarMode mode) {
    navigationConfigurationStorage.updateSidebarUserMode(username, mode);
  }

  /**
   * Updates the Navigation configuration
   * 
   * @param navigationConfiguration
   */
  public void updateConfiguration(NavigationConfiguration navigationConfiguration) {
    NavigationConfiguration existingConfiguration = getConfiguration();
    try {
      navigationConfigurationStorage.updateConfiguration(navigationConfiguration);
      listenerService.broadcast(NAVIGATION_CONFIGURATION_UPDATED_EVENT, existingConfiguration, navigationConfiguration);
    } finally {
      userPortalConfigService.setAllowUserHome(navigationConfiguration.getSidebar().isAllowUserCustomHome());
    }
  }

  /**
   * @return Default Topbar Applications as configured in {@link AddOnService}
   */
  public List<TopbarApplication> getDefaultTopbarApplications() {
    if (defaultTopbarApplications == null) {
      defaultTopbarApplications = addonContainerService.getApplications(TOP_NAVIGATION_ADDON_CONTAINER)
                                                       .stream()
                                                       .map(this::toTopbarApplication)
                                                       .toList();
    }
    return defaultTopbarApplications;
  }

  private TopbarApplication toTopbarApplication(Application app) {
    String portletName = app.getState() instanceof TransientApplicationState applicationState ?
                                                                                              applicationState.getContentId()
                                                                                                              .split("/")[1] :
                                                                                              "-";
    String applicationId = StringUtils.firstNonBlank(app.getId(), portletName);
    String applicationTitle = StringUtils.firstNonBlank(app.getTitle(), portletName);
    String applicationDescription = StringUtils.firstNonBlank(app.getDescription(), "-");
    String applicationIcon = StringUtils.firstNonBlank(app.getIcon(), "far fa-question-circle");
    return new TopbarApplication(applicationId,
                                 applicationTitle,
                                 applicationDescription,
                                 applicationIcon,
                                 TopbarItemType.APP,
                                 !StringUtils.equals(environment.getProperty(String.format(TOPBAR_APPLICATION_ENABLED_PATTERN,
                                                                                           app.getId()),
                                                                             "true"),
                                                     "false"),
                                 !StringUtils.equals(environment.getProperty(String.format(TOPBAR_APPLICATION_MOBILE_PATTERN,
                                                                                           app.getId()),
                                                                             "true"),
                                                     "false"),
                                 Collections.singletonMap(TopbarApplication.CONTENT_ID_PROP_NAME,
                                                          ((TransientApplicationState) app.getState()).getContentId()));
  }

  private SidebarMode getSidebarUserMode(String username, SidebarConfiguration sidebarConfiguration) {
    SidebarMode mode = navigationConfigurationStorage.getSidebarUserMode(username);
    if (sidebarConfiguration != null
        && (mode == null
            || !sidebarConfiguration.getAllowedModes().contains(mode))) {
      mode = sidebarConfiguration.getDefaultMode();
    }
    return mode;
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
