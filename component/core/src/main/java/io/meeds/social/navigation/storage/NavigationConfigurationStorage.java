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
package io.meeds.social.navigation.storage;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

import io.meeds.social.navigation.constant.TopbarItemType;
import io.meeds.social.navigation.model.NavigationConfiguration;
import io.meeds.social.navigation.model.TopbarApplication;
import io.meeds.social.util.JsonUtils;

@Component
public class NavigationConfigurationStorage {

  private static final String                  SETTING_KEY            = "configuration";

  private static final Scope                   SETTING_GLOBAL_SCOPE   = Scope.GLOBAL.id("NavigationConfiguration");

  private static final Context                 SETTING_GLOBAL_CONTEXT = Context.GLOBAL.id("NavigationConfiguration");

  private static final NavigationConfiguration NULL_VALUE             = new NavigationConfiguration();

  @Autowired
  private SettingService                       settingService;

  private NavigationConfiguration              navigationConfiguration;

  public NavigationConfiguration getConfiguration(List<TopbarApplication> defaultApplications) {
    if (navigationConfiguration == null) {
      navigationConfiguration = retrieveNavigationConfiguration(defaultApplications);
    }
    return navigationConfiguration == NULL_VALUE ? null : navigationConfiguration;
  }

  public void updateConfiguration(NavigationConfiguration navigationConfiguration) {
    try {
      settingService.set(SETTING_GLOBAL_CONTEXT,
                         SETTING_GLOBAL_SCOPE,
                         SETTING_KEY,
                         SettingValue.create(JsonUtils.toJsonString(navigationConfiguration)));
    } finally {
      this.navigationConfiguration = null;
    }
  }

  private NavigationConfiguration retrieveNavigationConfiguration(List<TopbarApplication> defaultApplications) {
    SettingValue<?> settingValue = settingService.get(SETTING_GLOBAL_CONTEXT, SETTING_GLOBAL_SCOPE, SETTING_KEY);
    if (settingValue == null || settingValue.getValue() == null) {
      return NULL_VALUE;
    } else {
      NavigationConfiguration configuration = JsonUtils.fromJsonString(settingValue.getValue().toString(),
                                                                       NavigationConfiguration.class);
      List<TopbarApplication> applications = configuration.getTopbar().getApplications();
      addMissingTopbarApplication(configuration, applications, defaultApplications);
      removeDroppedApplications(configuration, applications, defaultApplications);
      return configuration;
    }
  }

  /**
   * Remove applications which aren't available in addon container anymore
   * 
   * @param configuration
   * @param applications
   * @param addonContainerApplications
   */
  private void removeDroppedApplications(NavigationConfiguration configuration,
                                         List<TopbarApplication> applications,
                                         List<TopbarApplication> addonContainerApplications) {
    List<TopbarApplication> applicationsToRemove = applications.stream()
                                                               .filter(app -> app.getType() == TopbarItemType.APP
                                                                              && addonContainerApplications.stream()
                                                                                                           .noneMatch(containerApp -> StringUtils.equals(containerApp.getId(),
                                                                                                                                                         app.getId())))
                                                               .toList();
    if (CollectionUtils.isNotEmpty(applicationsToRemove)) {
      List<TopbarApplication> mergedApplications = new ArrayList<>(applications);
      mergedApplications.removeAll(applicationsToRemove);
      configuration.getTopbar().setApplications(mergedApplications);
    }
  }

  /**
   * Add applications which are newly made available in addon container
   * 
   * @param configuration
   * @param applications
   * @param addonContainerApplications
   */
  private void addMissingTopbarApplication(NavigationConfiguration configuration,
                                           List<TopbarApplication> applications,
                                           List<TopbarApplication> addonContainerApplications) {
    List<TopbarApplication> applicationsToAdd = addonContainerApplications.stream()
                                                                          .filter(app -> applications.stream()
                                                                                                     .noneMatch(containerApp -> StringUtils.equals(containerApp.getId(),
                                                                                                                                                   app.getId())))
                                                                          .toList();
    if (CollectionUtils.isNotEmpty(applicationsToAdd)) {
      List<TopbarApplication> mergedApplications = new ArrayList<>(applications);
      mergedApplications.addAll(applicationsToAdd);
      configuration.getTopbar().setApplications(mergedApplications);
    }
  }

}
