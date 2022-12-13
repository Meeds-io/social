/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2022 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.notification.plugin;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;

import lombok.Getter;

public abstract class SpaceWebNotificationPlugin extends BaseComponentPlugin {

  private IdentityManager identityManager;

  @Getter
  private List<String>    notificationPluginIds;

  protected SpaceWebNotificationPlugin(IdentityManager identityManager, InitParams params) {
    if (params == null) {
      throw new IllegalArgumentException("Params are mandatory");
    }
    this.identityManager = identityManager;
    notificationPluginIds = params.getValuesParam("notification.plugin.ids").getValues();
  }

  /**
   * Test if a given plugin is a managed plugin
   * 
   * @param  pluginKey whether {@link PluginKey} is managed or not
   * @return           true if managed, else false
   */
  public boolean isManagedPlugin(PluginKey pluginKey) {
    return notificationPluginIds.stream().anyMatch(pluginId -> StringUtils.equals(pluginKey.getId(), pluginId));
  }

  /**
   * Get the space application Item for a specific notification info and
   * username
   * 
   * @param  notification {@link NotificationInfo} which is triggered to notify
   *                        user
   * @param  username     concerned username by the notification
   * @return              SpaceWebNotificationItem
   */
  public SpaceWebNotificationItem getSpaceApplicationItem(NotificationInfo notification, String username) {
    SpaceWebNotificationItem notificationItem = getSpaceApplicationItem(notification);
    if (notificationItem != null && notificationItem.getUserId() == 0) {
      long userIdentityId = Long.parseLong(identityManager.getOrCreateUserIdentity(username).getId());
      notificationItem.setUserId(userIdentityId);
    }
    return notificationItem;
  }

  /**
   * Get the space application Item for a specific notification info
   * 
   * @param  notification
   * @return              SpaceWebNotificationItem
   */
  protected abstract SpaceWebNotificationItem getSpaceApplicationItem(NotificationInfo notification);

}
