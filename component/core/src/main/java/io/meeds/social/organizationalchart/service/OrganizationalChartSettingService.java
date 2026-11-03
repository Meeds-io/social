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
package io.meeds.social.organizationalchart.service;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.common.ContainerTransactional;

/**
 * Saves the settings of an organizational chart application: the user the
 * chart is centered on and whether its header title is displayed. The settings
 * are read by the application's portlet when it renders. They are edited by an
 * administrator, or by a manager of the space whose page holds the chart.
 */
@Service
public class OrganizationalChartSettingService {

  public static final String   OBJECT_TYPE                  = "organizationalChart";

  public static final String   CENTER_USER_SETTING_KEY      = "organizationalChartCenterUser";

  public static final String   HAS_HEADER_TITLE_SETTING_KEY = "organizationalChartHasHeaderTitle";

  /** The space whose page holds the chart, recorded by its portlet */
  public static final String   SPACE_ID_SETTING_KEY         = "organizationalChartSpaceId";

  /** The center user value that centers the chart on the user who views it */
  public static final String   CONNECTED_USER               = "@connected@";

  public static final String   INVALID_APPLICATION_MESSAGE  = "organizationalChart.invalidApplication";

  public static final String   INVALID_CENTER_USER_MESSAGE  = "organizationalChart.invalidCenterUser";

  private static final Pattern NUMERIC_PATTERN              = Pattern.compile("^\\d{1,18}$");

  @Autowired
  private SettingService       settingService;

  @Autowired
  private IdentityManager      identityManager;

  @Autowired
  private UserACL              userAcl;

  @Autowired
  private SpaceService         spaceService;

  /**
   * @param applicationId the id of the organizational chart application
   * @param centerUser the identity id of the user the chart is centered on, or
   *          {@link #CONNECTED_USER}, or null to keep the saved one
   * @param hasHeaderTitle whether the header title is displayed
   * @param username the user who saves the settings
   * @throws IllegalAccessException when the user can't edit the settings of
   *           this chart
   * @throws IllegalArgumentException when the application id or the center
   *           user is invalid
   */
  @ContainerTransactional(atomic = true)
  public void saveSettings(String applicationId,
                           String centerUser,
                           boolean hasHeaderTitle,
                           String username) throws IllegalAccessException {
    saveSettingsInTransaction(applicationId, centerUser, hasHeaderTitle, username);
  }

  void saveSettingsInTransaction(String applicationId,
                                 String centerUser,
                                 boolean hasHeaderTitle,
                                 String username) throws IllegalAccessException {
    if (!canEdit(applicationId, username)) {
      throw new IllegalAccessException("User " + username + " can't edit the organizational chart settings");
    }
    if (applicationId == null || !NUMERIC_PATTERN.matcher(applicationId).matches()) {
      throw new IllegalArgumentException(INVALID_APPLICATION_MESSAGE);
    }
    Scope scope = Scope.APPLICATION.id(OBJECT_TYPE + applicationId);
    if (centerUser != null && !isStoredCenterUser(scope, centerUser) && !isValidCenterUser(centerUser)) {
      throw new IllegalArgumentException(INVALID_CENTER_USER_MESSAGE);
    }
    if (centerUser != null) {
      settingService.set(Context.GLOBAL, scope, CENTER_USER_SETTING_KEY, SettingValue.create(centerUser));
    }
    settingService.set(Context.GLOBAL, scope, HAS_HEADER_TITLE_SETTING_KEY, SettingValue.create(String.valueOf(hasHeaderTitle)));
  }

  /**
   * @param applicationId the id of the organizational chart application
   * @param username the user to check
   * @return true when the user is an administrator, or a manager of the space
   *         recorded for the chart by {@link #saveApplicationSpace}
   */
  public boolean canEdit(String applicationId, String username) {
    if (StringUtils.isBlank(username)) {
      return false;
    }
    if (userAcl.isAdministrator(userAcl.getUserIdentity(username))) {
      return true;
    }
    Space space = getApplicationSpace(applicationId);
    return space != null && spaceService.canManageSpace(space, username);
  }

  /**
   * Records, once, the space whose page holds the chart. Only the window whose
   * storage id is the application id records it: the application id is the
   * storage id of the window that rendered first, a page editor can set the
   * preference that holds it on any window, and publishing a page gives its
   * windows new storage ids while copying their preferences.
   *
   * @param applicationId the id of the organizational chart application
   * @param windowStorageId the storage id of the window that renders
   * @param pageSite the site of the page that holds the window, null when the
   *          window is in a site or shared layout
   */
  public void saveApplicationSpace(String applicationId, String windowStorageId, SiteKey pageSite) {
    if (applicationId == null
        || !NUMERIC_PATTERN.matcher(applicationId).matches()
        || !applicationId.equals(windowStorageId)
        || pageSite == null
        || pageSite.getType() != SiteType.GROUP) {
      return;
    }
    Scope scope = Scope.APPLICATION.id(OBJECT_TYPE + applicationId);
    if (getSettingValue(scope, SPACE_ID_SETTING_KEY) != null) {
      return;
    }
    Space space = spaceService.getSpaceByGroupId(pageSite.getName());
    if (space != null) {
      settingService.set(Context.GLOBAL, scope, SPACE_ID_SETTING_KEY, SettingValue.create(space.getId()));
    }
  }

  private Space getApplicationSpace(String applicationId) {
    if (applicationId == null || !NUMERIC_PATTERN.matcher(applicationId).matches()) {
      return null;
    }
    String spaceId = getSettingValue(Scope.APPLICATION.id(OBJECT_TYPE + applicationId), SPACE_ID_SETTING_KEY);
    return spaceId == null || !NUMERIC_PATTERN.matcher(spaceId).matches() ? null : spaceService.getSpaceById(spaceId);
  }

  private String getSettingValue(Scope scope, String key) {
    SettingValue<?> storedValue = settingService.get(Context.GLOBAL, scope, key);
    return storedValue == null || storedValue.getValue() == null ? null : storedValue.getValue().toString();
  }

  /**
   * The center user stored before keeps being accepted even when the user was
   * disabled since, as the settings drawer sends it back with every save
   */
  private boolean isStoredCenterUser(Scope scope, String centerUser) {
    return centerUser.equals(getSettingValue(scope, CENTER_USER_SETTING_KEY));
  }

  private boolean isValidCenterUser(String centerUser) {
    if (CONNECTED_USER.equals(centerUser)) {
      return true;
    }
    if (!NUMERIC_PATTERN.matcher(centerUser).matches()) {
      return false;
    }
    Identity identity = identityManager.getIdentity(centerUser);
    return identity != null
           && OrganizationIdentityProvider.NAME.equals(identity.getProviderId())
           && identity.isEnable()
           && !identity.isDeleted();
  }

}
