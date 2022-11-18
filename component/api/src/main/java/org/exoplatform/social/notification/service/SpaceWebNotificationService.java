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
package org.exoplatform.social.notification.service;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;
import org.exoplatform.social.notification.plugin.SpaceWebNotificationPlugin;

import java.util.Map;

public interface SpaceWebNotificationService {

  /**
   * Register the space web notification plugin
   * 
   * @param spaceWebNotification
   */
  void addPlugin(SpaceWebNotificationPlugin spaceWebNotification);

  /**
   * Dispatch notification info
   * 
   * @param notification
   * @param username
   * @throws Exception
   */
  void dispatch(NotificationInfo notification, String username) throws Exception;

  /**
   * Mark a space notification item as unread
   * 
   * @param notificationItem
   * @throws Exception
   */
  void markAsUnread(SpaceWebNotificationItem notificationItem) throws Exception;

  /**
   * Mark a space notification item as read
   * 
   * @param notificationItem
   * @throws Exception
   */
  void markAsRead(SpaceWebNotificationItem notificationItem) throws Exception;

  /**
   * Marka a list of unread items per application as read to a given {@link MetadataItem} creatorId
   * by a given {@link Space} identifier
   *
   * @param userIdentityId {@link MetadataItem} creatorId
   * @throws Exception
   * @spaceId spaceId {@link Space} spaceId
   */
  void markAllAsRead(long userIdentityId, long spaceId) throws Exception;

  /**
   * Get a list of unread items per application to a given {@link MetadataItem} creatorId
   * by a given {@link Space} identifier
   *
   * @param creatorId {@link MetadataItem} creatorId
   * @spaceId spaceId {@link Space} spaceId
   * @return Map of application and unread items
   */
  Map<String, Long> countUnreadItemsByApplication(long creatorId, long spaceId);

}