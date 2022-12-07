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
package org.exoplatform.social.notification.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;
import org.exoplatform.social.notification.plugin.SpaceWebNotificationPlugin;
import org.exoplatform.social.notification.service.SpaceWebNotificationService;

public class SpaceWebNotificationServiceImpl implements SpaceWebNotificationService {

  public static final String               METADATA_TYPE_NAME               = "unread";

  public static final String               NOTIFICATION_UNREAD_EVENT_NAME   = "notification.unread.item";

  public static final String               NOTIFICATION_READ_EVENT_NAME     = "notification.read.item";

  public static final String               NOTIFICATION_ALL_READ_EVENT_NAME = "notification.read.allItems";

  private static final Log                 LOG                              =
                                               ExoLogger.getLogger(SpaceWebNotificationServiceImpl.class);

  private MetadataService                  metadataService;

  private ListenerService                  listenerService;

  private boolean                          enabled;

  private List<SpaceWebNotificationPlugin> plugins                          = new ArrayList<>();

  public SpaceWebNotificationServiceImpl(MetadataService metadataService,
                                         ExoFeatureService featureService,
                                         ListenerService listenerService) {
    this.metadataService = metadataService;
    this.listenerService = listenerService;
    this.enabled = featureService.isActiveFeature("SpaceWebNotifications");
  }

  @Override
  public void addPlugin(SpaceWebNotificationPlugin spaceWebNotification) {
    plugins.add(0, spaceWebNotification);
  }

  @Override
  public void dispatch(NotificationInfo notification, String username) throws Exception {
    if (!this.enabled) {
      LOG.debug("Space Web Notifications are disabled, thus no notification will be sent.");
      return;
    }

    SpaceWebNotificationPlugin spaceWebNotification = plugins.stream()
                                                             .filter(plugin -> plugin.isManagedPlugin(notification.getKey()))
                                                             .findFirst()
                                                             .orElse(null);
    if (spaceWebNotification != null) {
      SpaceWebNotificationItem notificationItem = spaceWebNotification.getSpaceApplicationItem(notification, username);
      markAsUnread(notificationItem);
    }
  }

  @Override
  public void markAsUnread(SpaceWebNotificationItem notificationItem) throws Exception {
    if (!this.enabled) {
      LOG.debug("Space Web Notifications are disabled, thus no notification will be sent.");
      return;
    }

    long userIdentityId = notificationItem.getUserId();
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME, String.valueOf(userIdentityId), userIdentityId);
    MetadataObject metadataObject = new MetadataObject(notificationItem.getApplicationName(),
                                                       notificationItem.getApplicationItemId(),
                                                       null,
                                                       notificationItem.getSpaceId());
    metadataService.createMetadataItem(metadataObject, metadataKey, userIdentityId);
    listenerService.broadcast(NOTIFICATION_UNREAD_EVENT_NAME, notificationItem, userIdentityId);
  }

  @Override
  public void markAsRead(SpaceWebNotificationItem notificationItem) throws Exception {
    if (notificationItem == null) {
      throw new IllegalArgumentException("SpaceWebNotificationItem is mandatory");
    }
    long userIdentityId = notificationItem.getUserId();
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME, String.valueOf(userIdentityId), userIdentityId);
    MetadataObject metadataObject = new MetadataObject(notificationItem.getApplicationName(),
                                                       notificationItem.getApplicationItemId(),
                                                       null,
                                                       notificationItem.getSpaceId());

    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataAndObject(metadataKey, metadataObject);
    if (CollectionUtils.isNotEmpty(metadataItems)) {
      for (MetadataItem metadataItem : metadataItems) {
        metadataService.deleteMetadataItem(metadataItem.getId(), true);
      }
      listenerService.broadcast(NOTIFICATION_READ_EVENT_NAME, notificationItem, userIdentityId);
    }
  }

  @Override
  public void markAllAsRead(long userIdentityId, long spaceId) throws Exception {
    metadataService.deleteMetadataBySpaceIdAndAudienceId(spaceId, userIdentityId);
    listenerService.broadcast(NOTIFICATION_ALL_READ_EVENT_NAME, userIdentityId, spaceId);
  }

  @Override
  public Map<String, Long> countUnreadItemsByApplication(long creatorId, long spaceId) {
    return metadataService.countMetadataItemsByMetadataTypeAndAudienceId(METADATA_TYPE_NAME, creatorId, spaceId);
  }

}
