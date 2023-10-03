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
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.mock;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;
import org.exoplatform.social.notification.plugin.SpaceWebNotificationPlugin;
import org.exoplatform.social.notification.service.SpaceWebNotificationService;

public class SpaceWebNotificationServiceMock implements SpaceWebNotificationService {

  @Override
  public void addPlugin(SpaceWebNotificationPlugin spaceWebNotification) {
    // Empty operation
  }

  @Override
  public void dispatch(NotificationInfo notification, String username) throws Exception {
    // Empty operation
  }

  @Override
  public void markAsUnread(SpaceWebNotificationItem notificationItem) throws Exception {
    // Empty operation
  }

  @Override
  public void markAsRead(SpaceWebNotificationItem notificationItem) throws Exception {
    // Empty operation
  }

  @Override
  public void markAllAsRead(long userIdentityId, long spaceId) throws Exception {
    // Empty operation
  }

  @Override
  public void markAllAsRead(long userIdentityId) throws Exception {
    // Empty operation
  }

  @Override
  public Map<String, Long> countUnreadItemsByApplication(long creatorId, long spaceId) {
    return Collections.emptyMap();
  }

  @Override
  public Map<Long, Long> countUnreadItemsBySpace(String username) {
    return Collections.emptyMap();
  }

  @Override
  public List<Long> getUnreadActivityIds(String username, long offset, long limit) {
    return Collections.emptyList();
  }

  @Override
  public List<Long> getUnreadActivityIdsBySpace(String username,
                                                long spaceId,
                                                long offset,
                                                long limit) throws IllegalAccessException, ObjectNotFoundException {
    return Collections.emptyList();
  }

  @Override
  public long countUnreadActivities(String username) {
    return 0l;
  }

  @Override
  public long countUnreadActivitiesBySpace(String username, long spaceId) throws IllegalAccessException,
                                                                           ObjectNotFoundException {
    return 0l;
  }

}
