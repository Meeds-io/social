/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.webNotification.service.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Feb 3, 2015  
 */
public class MockWebNotificationStorage implements WebNotificationStorage {
  
  private static final Random RANDOM = new Random();

  private Map<String, NotificationInfo> map = new HashMap<>();

  public Map<String, NotificationInfo> getMap() {
    return this.map;
  }

  @Override
  public void save(NotificationInfo notification) {
    notification.setId(String.valueOf(RANDOM.nextLong()));
    this.map.put(notification.getId(), notification);
  }

  @Override
  public void update(NotificationInfo notification, boolean moveTop) {
    // No behavior
  }

  @Override
  public void markRead(String notificationId) {
    // No behavior
  }

  @Override
  public void markAllRead(String userId) {
    // No behavior
  }

  @Override
  public void hidePopover(String notificationId) {
    // No behavior
  }

  @Override
  public List<NotificationInfo> get(WebNotificationFilter filter, int offset, int limit) {
    return new ArrayList<>(this.map.values());
  }

  @Override
  public NotificationInfo get(String notificationId) {
    return this.map.get(notificationId);
  }

  @Override
  public boolean remove(String notificationId) {
    if (notificationId == null) {
      this.map.clear();
    } else {
      this.map.remove(notificationId);
    }
    return false;
  }

  @Override
  public boolean remove(String userId, long seconds) {
    return false;
  }

  @Override
  public NotificationInfo getUnreadNotification(String pluginId, String activityId, String owner) {
    return null;
  }

  @Override
  public int getNumberOnBadge(String userId) {
    return 0;
  }

  @Override
  public void resetNumberOnBadge(String userId) {
    // No behavior
  }

  @Override
  public boolean remove(long seconds) {
    return false;
  }

  @Override
  public void markAllRead(List<String> plugins, String username) {
    // No behavior
  }

  @Override
  public Map<String, Integer> countUnreadByPlugin(String userId) {
    return Collections.emptyMap();
  }

  @Override
  public void resetNumberOnBadge(List<String> plugins, String username) {
    // No behavior
  }
}
