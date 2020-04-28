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
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.storage.MailNotificationStorage;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;

import java.util.List;
import java.util.Map;

public class MockNotificationDataStorage implements MailNotificationStorage {
  NotificationService notificationService;
  public MockNotificationDataStorage(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Override
  public void save(NotificationInfo notification) throws Exception {
  }

  @Override
  public void removeMessageAfterSent(NotificationContext context) throws Exception {
    
  }

  @Override
  public Map<PluginKey, List<NotificationInfo>> getByUser(NotificationContext context,
                                                                UserSetting userSetting) {
    return null;
  }


}
