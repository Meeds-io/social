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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.meeds.common.ContainerTransactional;
import io.meeds.social.navigation.constant.SidebarMode;
import io.meeds.social.navigation.model.NavigationConfiguration;
import io.meeds.social.navigation.model.SidebarConfiguration;
import io.meeds.social.navigation.model.SidebarItem;
import io.meeds.social.navigation.model.TopbarConfiguration;
import io.meeds.social.navigation.plugin.SidebarPlugin;

import jakarta.annotation.PostConstruct;

/**
 * A Service to inject Default Left Menu configuration on server startup
 */
@Component
public class NavigationConfigurationInitService {

  @Autowired
  private NavigationConfigurationService navigationConfigurationService;

  @Autowired
  private List<SidebarPlugin>            menuPlugins;

  @Value("${navigation.configuration.forceUpdate:false}")
  private boolean                        forceUpdate;

  @Value("${navigation.configuration.displayCompanyName:true}")
  private boolean                        displayCompanyName;

  @Value("${navigation.configuration.displayMobileCompanyLogo:true}")
  private boolean                        displayMobileCompanyLogo;

  @Value("${navigation.configuration.displaySiteName:true}")
  private boolean                        displaySiteName;

  @Value("${navigation.configuration.allowUserCustomHome:true}")
  private boolean                        allowUserCustomHome;

  @Value("${navigation.configuration.defaultMode:ICON}")
  private SidebarMode                    defaultMode;

  @PostConstruct
  @ContainerTransactional
  public void init() {
    if (forceUpdate || navigationConfigurationService.getConfiguration() == null) {
      navigationConfigurationService.updateConfiguration(buildDefaultNavigationConfiguration());
    }
  }

  private NavigationConfiguration buildDefaultNavigationConfiguration() {
    TopbarConfiguration topbarConfiguration = new TopbarConfiguration(displayCompanyName,
                                                                      displaySiteName,
                                                                      displayMobileCompanyLogo,
                                                                      navigationConfigurationService.getDefaultTopbarApplications());
    SidebarConfiguration sidebarConfiguration = new SidebarConfiguration(allowUserCustomHome,
                                                                         defaultMode,
                                                                         null,
                                                                         Arrays.asList(SidebarMode.values()),
                                                                         getDefaultNavigations());
    return new NavigationConfiguration(topbarConfiguration, sidebarConfiguration);
  }

  /**
   * @return Default Sites Navigations
   */
  private List<SidebarItem> getDefaultNavigations() {
    if (menuPlugins != null) {
      return menuPlugins.stream()
                        .map(SidebarPlugin::getDefaultItems)
                        .flatMap(items -> {
                          if (CollectionUtils.isEmpty(items)) {
                            return Stream.empty();
                          } else {
                            return items.stream();
                          }
                        })
                        .toList();
    } else {
      return Collections.emptyList();
    }
  }

}
