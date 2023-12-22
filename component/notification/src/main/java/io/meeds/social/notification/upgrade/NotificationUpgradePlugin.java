/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
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
package io.meeds.social.notification.upgrade;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class NotificationUpgradePlugin extends UpgradeProductPlugin {

  private static final Log     LOG                                 = ExoLogger.getLogger(NotificationUpgradePlugin.class);

  private static final String  NOTIFICATION_PLUGIN_ID_LIKE_PARAM   = "pluginIdPart";

  private static final String  NOTIFICATION_PLUGIN_ID_CONCAT_PARAM = "pluginIdToConcat";

  private static final String  NOTIFICATION_CHANNEL_IDS_PARAM      = "channelIds";

  private static final String  COUNT_UPGRADED_USERS_QUERY          =
                                                          "SELECT COUNT(DISTINCT s.context.id) FROM SettingsEntity s WHERE s.value LIKE :"
                                                              + NOTIFICATION_PLUGIN_ID_LIKE_PARAM;

  private static final String  COUNT_NOT_UPGRADED_USERS_QUERY      =
                                                              "SELECT COUNT(DISTINCT s.context.id) FROM SettingsEntity s WHERE s.name LIKE 'exo:%Channel' AND s.value NOT LIKE :"
                                                                  + NOTIFICATION_PLUGIN_ID_LIKE_PARAM;

  private static final String  UPGRADE_USERS_NOTIFICATIONS_QUERY   =
                                                                 "UPDATE SettingsEntity s SET s.value = CONCAT(s.value, :"
                                                                     + NOTIFICATION_PLUGIN_ID_CONCAT_PARAM
                                                                     + ") WHERE s.name IN (:" + NOTIFICATION_CHANNEL_IDS_PARAM
                                                                     + ") AND s.value NOT LIKE :"
                                                                     + NOTIFICATION_PLUGIN_ID_LIKE_PARAM;

  private EntityManagerService entityManagerService;

  private UserSettingService   userSettingService;

  private String               notificationChannelId;

  private String               notificationPluginId;

  public NotificationUpgradePlugin(EntityManagerService entityManagerService,
                                   UserSettingService userSettingService,
                                   SettingService settingService,
                                   InitParams initParams) {
    super(settingService, initParams);
    this.entityManagerService = entityManagerService;
    this.userSettingService = userSettingService;
    this.notificationChannelId = getParamValue(initParams, "notificationChannelId");
    this.notificationPluginId = getParamValue(initParams, "notificationPluginId");
  }

  @Override
  public void beforeUpgrade() {
    if (PropertyManager.isDevelopping()
        && !StringUtils.equals(getName(), notificationPluginId)) {
      LOG.warn("Upgrade Plugin Name '{}' should use the same name as Notification Plugin Identifier '{}'",
               getName(),
               notificationPluginId);
    }
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && StringUtils.isNotBlank(notificationPluginId);
  }

  @Override
  public boolean isExecuteOnlyOnce() {
    // Execute only once all time for the same Upgrade Plugin
    // using a specific plugin name
    return true;
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    int usersToUpgradeCount = countUsersToUpgrade();
    if (usersToUpgradeCount == 0) {
      LOG.debug("Notification Plugin '{}' Setting upgrade will not proceed since no user has specific Notifications settings.",
                notificationPluginId);
    } else {
      LOG.info("Start:: Upgrade Notification Plugin Setting '{}' for {} users.", notificationPluginId, usersToUpgradeCount);
      Set<String> activeChannels =
                                 StringUtils.isBlank(notificationChannelId) ? userSettingService.getDefaultSettings()
                                                                                                .getChannelActives()
                                                                            : Collections.singleton(notificationChannelId);
      upgradeUsersNotifications(activeChannels);
      int upgradedUsers = countUpgradedUsers();
      if (upgradedUsers < usersToUpgradeCount) {
        throw new IllegalStateException(String.format("End:: Upgrade Notification Plugin Setting '%s' didn't upgraded all users settings: %s/%s",
                                                      notificationPluginId,
                                                      upgradedUsers,
                                                      usersToUpgradeCount));
      }
      LOG.info("End:: Upgrade Notification Plugin Setting '{}' for {}/{} users.",
               notificationPluginId,
               upgradedUsers,
               usersToUpgradeCount);
    }
  }

  @ExoTransactional
  public int upgradeUsersNotifications(Set<String> channelIds) {
    List<String> channelValues = channelIds.stream().map(channelId -> "exo:" + channelId + "Channel").toList();
    EntityManager entityManager = entityManagerService.getEntityManager();
    Query query = entityManager.createQuery(UPGRADE_USERS_NOTIFICATIONS_QUERY);
    query.setParameter(NOTIFICATION_CHANNEL_IDS_PARAM, channelValues);
    query.setParameter(NOTIFICATION_PLUGIN_ID_LIKE_PARAM, "%" + notificationPluginId + "%");
    query.setParameter(NOTIFICATION_PLUGIN_ID_CONCAT_PARAM, "," + notificationPluginId);
    return query.executeUpdate();
  }

  @ExoTransactional
  public int countUpgradedUsers() {
    EntityManager entityManager = entityManagerService.getEntityManager();
    TypedQuery<Long> query = entityManager.createQuery(COUNT_UPGRADED_USERS_QUERY, Long.class);
    query.setParameter(NOTIFICATION_PLUGIN_ID_LIKE_PARAM, "%" + notificationPluginId + "%");
    try {
      Long queryResult = query.getSingleResult();
      return queryResult == null ? 0 : queryResult.intValue();
    } catch (NoResultException e) {
      return 0;
    }
  }

  @ExoTransactional
  public int countUsersToUpgrade() {
    EntityManager entityManager = entityManagerService.getEntityManager();
    TypedQuery<Long> query = entityManager.createQuery(COUNT_NOT_UPGRADED_USERS_QUERY, Long.class);
    query.setParameter(NOTIFICATION_PLUGIN_ID_LIKE_PARAM, "%" + notificationPluginId + "%");
    try {
      Long queryResult = query.getSingleResult();
      return queryResult == null ? 0 : queryResult.intValue();
    } catch (NoResultException e) {
      return 0;
    }
  }

  private String getParamValue(InitParams initParams, String paramName) {
    if (initParams == null || !initParams.containsKey(paramName)) {
      return null;
    } else {
      return initParams.getValueParam(paramName).getValue();
    }
  }
}
