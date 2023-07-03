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

import java.util.Arrays;

import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.social.notification.AbstractNotificationCoreTest;

public class NotificationUpgradePluginTest extends AbstractNotificationCoreTest {// NOSONAR

  private static final String DEFAULT_NOTIFICATION_PLUGIN_ID = "DefaultNotificationPluginId";

  private static final String UPGRADE_NOTIFICATION_PLUGIN    = "UpgradeNotificationPlugin";

  private UserSettingService  userSettingService;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    userSettingService = getContainer().getComponentInstanceOfType(UserSettingService.class);
  }

  public void testNotificationUpgrade() {
    UserSetting userSettings = userSettingService.get("root");
    String channelId = userSettings.getChannelActives().iterator().next();
    userSettings.setChannelPlugins(channelId, Arrays.asList(DEFAULT_NOTIFICATION_PLUGIN_ID));
    userSettingService.save(userSettings);
    UserSetting rootUserSettings = userSettingService.get(userSettings.getUserId());
    assertTrue(rootUserSettings.getPlugins(channelId).contains(DEFAULT_NOTIFICATION_PLUGIN_ID));

    InitParams initParams = new InitParams();

    ValueParam valueParam = new ValueParam();
    valueParam.setName("product.group.id");
    valueParam.setValue("org.exoplatform.social");
    initParams.addParam(valueParam);

    valueParam = new ValueParam();
    valueParam.setName("plugin.execution.order");
    valueParam.setValue("5");
    initParams.addParam(valueParam);

    valueParam = new ValueParam();
    valueParam.setName("notificationPluginId");
    valueParam.setValue(UPGRADE_NOTIFICATION_PLUGIN);
    initParams.addParam(valueParam);

    EntityManagerService entityManagerService = getContainer().getComponentInstanceOfType(EntityManagerService.class);
    SettingService settingService = getContainer().getComponentInstanceOfType(SettingService.class);
    NotificationUpgradePlugin upgradePlugin = new NotificationUpgradePlugin(entityManagerService,
                                                                            userSettingService,
                                                                            settingService,
                                                                            initParams);
    upgradePlugin.setName(UPGRADE_NOTIFICATION_PLUGIN);
    assertTrue(upgradePlugin.isEnabled());

    upgradePlugin.processUpgrade(null, null);
    restartTransaction();

    rootUserSettings = userSettingService.get(userSettings.getUserId());
    assertTrue(rootUserSettings.getPlugins(channelId).contains(UPGRADE_NOTIFICATION_PLUGIN));
  }

}
