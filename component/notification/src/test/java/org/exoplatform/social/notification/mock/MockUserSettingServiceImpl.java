/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.notification.mock;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.services.organization.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockUserSettingServiceImpl implements UserSettingService {

  private Map<String, UserSetting> settings = new HashMap<String, UserSetting>();
  
  public MockUserSettingServiceImpl() {
    
  }
  
  @Override
  public void save(UserSetting setting) {
    settings.put(setting.getUserId(), setting);
  }

  @Override
  public UserSetting get(String userId) {
    return settings.get(userId);
  }

  public List<String> getUserSettingByPlugin(String pluginId) {
    List<String> userIds = new ArrayList<String>();
    for (UserSetting userSetting : settings.values()) {
      if (userSetting.isInDaily(pluginId) 
          || userSetting.isInWeekly(pluginId) 
          || userSetting.isActive(UserSetting.EMAIL_CHANNEL, pluginId)) {
        userIds.add(userSetting.getUserId());
      }
    }
    
    return userIds;
  }

  public void initDefaultSettings(String userId) {
  }

  public void initDefaultSettings(User[] users) {
  }

  @Override
  public UserSetting getDefaultSettings() {
    return new UserSetting();
  }

  @Override
  public List<UserSetting> getDigestSettingForAllUser(NotificationContext context,
                                                      int offset,
                                                      int limit) {
    return null;
  }

  @Override
  public List<UserSetting> getDigestDefaultSettingForAllUser(int offset, int limit) {
    return null;
  }

  public List<String> getUserHasSettingPlugin(String channelId, String pluginId) {
    return getUserSettingByPlugin(pluginId);
  }

  @Override
  public void saveLastReadDate(String userId, Long time) {
  }

  public void setUserEnabled(String username, boolean enabled) {
  }

}
