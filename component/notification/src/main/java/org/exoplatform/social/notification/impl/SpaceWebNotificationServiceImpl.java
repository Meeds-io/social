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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.ServletContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer.PortalContainerPostInitTask;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.notification.channel.SpaceWebChannel;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;
import org.exoplatform.social.notification.plugin.SpaceWebNotificationPlugin;
import org.exoplatform.social.notification.service.SpaceWebNotificationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;

@SuppressWarnings("removal")
public class SpaceWebNotificationServiceImpl implements SpaceWebNotificationService, Startable {

  private static final String              SPACE_NOT_FOUND               = "Space with id %s doesn't exist";

  private static final String              USER_NOT_MEMBER_OF_SPACE      = "User %s isn't member of space %s";

  public static final String               APPLICATION_SUB_ITEM_IDS      = "applicationSubItemIds";

  public static final String               METADATA_TYPE_NAME            = "unread";

  public static final String               METADATA_ACTIVITY_ID_PROPERTY = "activityId";

  private static final Log                 LOG                           =
                                               ExoLogger.getLogger(SpaceWebNotificationServiceImpl.class);

  private PortalContainer                  container;

  private MetadataService                  metadataService;

  private ListenerService                  listenerService;

  private PluginSettingService             pluginSettingService;

  private IdentityManager                  identityManager;

  private SpaceService                     spaceService;

  private List<SpaceWebNotificationPlugin> plugins                       = new ArrayList<>();

  public SpaceWebNotificationServiceImpl(PortalContainer container,
                                         MetadataService metadataService,
                                         PluginSettingService pluginSettingService,
                                         ListenerService listenerService,
                                         IdentityManager identityManager,
                                         SpaceService spaceService) {
    this.container = container;
    this.metadataService = metadataService;
    this.listenerService = listenerService;
    this.pluginSettingService = pluginSettingService;
    this.identityManager = identityManager;
    this.spaceService = spaceService;
  }

  @Override
  public void start() { // NOSONAR
    PortalContainer.addInitTask(container.getPortalContext(), new PortalContainerPostInitTask() {
      @Override
      public void execute(ServletContext context, PortalContainer portalContainer) {
        for (SpaceWebNotificationPlugin spaceWebNotificationPlugin : plugins) {
          List<String> notificationPluginIds = spaceWebNotificationPlugin.getNotificationPluginIds();
          if (CollectionUtils.isNotEmpty(notificationPluginIds)) {
            for (String notificationPluginId : notificationPluginIds) {
              PluginConfig pluginConfig = pluginSettingService.getPluginConfig(notificationPluginId);
              if (pluginConfig == null) {
                LOG.warn("Notification plugin {} wasn't found", notificationPluginId);
              } else {
                pluginConfig.addAdditionalChannel(SpaceWebChannel.ID);
              }
            }
          }
        }
      }
    });
  }

  @Override
  public void stop() {
    // Nothing to stop
  }

  @Override
  public void addPlugin(SpaceWebNotificationPlugin spaceWebNotification) {
    plugins.add(0, spaceWebNotification);
  }

  @Override
  public void dispatch(NotificationInfo notification, String username) throws Exception {
    SpaceWebNotificationItem notificationItem = getSpaceWebNotificationItem(notification, username);
    if (notificationItem != null) {
      markAsUnread(notificationItem);
    }
  }

  @Override
  public void markAsUnread(SpaceWebNotificationItem notificationItem) throws Exception {
    if (notificationItem == null) {
      throw new IllegalArgumentException("SpaceWebNotificationItem is mandatory");
    }
    if (notificationItem.getSpaceId() <= 0) {
      throw new IllegalArgumentException("SpaceWebNotificationItem.spaceId is mandatory");
    }
    if (notificationItem.getUserId() <= 0) {
      throw new IllegalArgumentException("SpaceWebNotificationItem.userId is mandatory");
    }
    if (StringUtils.isBlank(notificationItem.getApplicationName())) {
      throw new IllegalArgumentException("SpaceWebNotificationItem.applicationName is mandatory");
    }
    if (StringUtils.isBlank(notificationItem.getApplicationItemId())) {
      throw new IllegalArgumentException("SpaceWebNotificationItem.applicationItemId is mandatory");
    }

    long userIdentityId = notificationItem.getUserId();
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME, String.valueOf(userIdentityId), userIdentityId);
    MetadataObject metadataObject = new MetadataObject(notificationItem.getApplicationName(),
                                                       notificationItem.getApplicationItemId(),
                                                       null,
                                                       notificationItem.getSpaceId());
    try {
      mergeExistingUnreadProperties(notificationItem, metadataKey, metadataObject);
      metadataService.createMetadataItem(metadataObject,
                                         metadataKey,
                                         toMetadataProperties(notificationItem),
                                         userIdentityId);
      listenerService.broadcast(NOTIFICATION_UNREAD_EVENT_NAME, notificationItem, userIdentityId);
    } catch (ObjectAlreadyExistsException e) {// NOSONAR
      LOG.debug("Object {} already marked as unread", notificationItem);
    }
  }

  @Override
  public void markAsRead(SpaceWebNotificationItem notificationItem) throws Exception {
    if (notificationItem == null) {
      throw new IllegalArgumentException("SpaceWebNotificationItem is mandatory");
    }
    if (notificationItem.getSpaceId() <= 0) {
      throw new IllegalArgumentException("SpaceWebNotificationItem.spaceId is mandatory");
    }
    if (notificationItem.getUserId() <= 0) {
      throw new IllegalArgumentException("SpaceWebNotificationItem.userId is mandatory");
    }
    if (StringUtils.isBlank(notificationItem.getApplicationName())) {
      throw new IllegalArgumentException("SpaceWebNotificationItem.applicationName is mandatory");
    }
    if (StringUtils.isBlank(notificationItem.getApplicationItemId())) {
      throw new IllegalArgumentException("SpaceWebNotificationItem.applicationItemId is mandatory");
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
  public void markAllAsRead(long userIdentityId) throws Exception {
    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("userIdentityId is mandatory");
    }
    metadataService.deleteByMetadataTypeAndCreatorId(METADATA_TYPE_NAME, userIdentityId);
    listenerService.broadcast(NOTIFICATION_ALL_READ_EVENT_NAME,
                              new SpaceWebNotificationItem(null, null, userIdentityId, 0l),
                              userIdentityId);
  }

  @Override
  public void markAllAsRead(long userIdentityId, long spaceId) throws Exception {
    if (spaceId <= 0) {
      throw new IllegalArgumentException("spaceId is mandatory");
    }
    if (userIdentityId <= 0) {
      throw new IllegalArgumentException("userIdentityId is mandatory");
    }
    metadataService.deleteByMetadataTypeAndSpaceIdAndCreatorId(METADATA_TYPE_NAME, spaceId, userIdentityId);
    listenerService.broadcast(NOTIFICATION_ALL_READ_EVENT_NAME,
                              new SpaceWebNotificationItem(null, null, userIdentityId, spaceId),
                              userIdentityId);
  }

  @Override
  public Map<String, Long> countUnreadItemsByApplication(long userIdentityId, long spaceId) {
    return metadataService.countMetadataItemsByMetadataTypeAndAudienceId(METADATA_TYPE_NAME, userIdentityId, spaceId);
  }

  @Override
  public List<Long> getUnreadActivityIds(String username, long offset, long limit) {
    Identity userIdentity = getUserIdentity(username);
    List<String> spaceIds = spaceService.getMemberSpacesIds(username, 0, -1);
    if (CollectionUtils.isEmpty(spaceIds)) {
      return Collections.emptyList();
    }
    return getUnreadActivityIds(offset, limit, spaceIds, userIdentity);
  }

  @Override
  public List<Long> getUnreadActivityIdsBySpace(String username,
                                                long spaceId,
                                                long offset,
                                                long limit) throws IllegalAccessException, ObjectNotFoundException {
    Identity userIdentity = getUserIdentity(username);
    String spaceIdString = String.valueOf(spaceId);
    Space space = spaceService.getSpaceById(spaceIdString);
    if (space == null) {
      throw new ObjectNotFoundException(String.format(SPACE_NOT_FOUND, spaceId));
    }
    if (!spaceService.isSuperManager(username) && !spaceService.isMember(space, username)
        && !spaceService.isManager(space, username)) {
      throw new IllegalAccessException(String.format(USER_NOT_MEMBER_OF_SPACE, username, spaceId));
    }
    return getUnreadActivityIds(offset, limit, Collections.singletonList(spaceIdString), userIdentity);
  }

  @Override
  public Map<Long, Long> countUnreadItemsBySpace(String username) {
    List<Long> spaceIds = new ArrayList<>();
    Identity userIdentity = getUserIdentity(username);
    List<String> spacesId = spaceService.getMemberSpacesIds(username, 0, -1);
    if (CollectionUtils.isNotEmpty(spacesId)) {
      spaceIds = spacesId.stream().map(Long::parseLong).toList();
    }
    return metadataService.countMetadataItemsByMetadataTypeAndSpacesIdAndCreatorId(METADATA_TYPE_NAME,
                                                                                   Long.parseLong(userIdentity.getId()),
                                                                                   spaceIds);
  }

  @Override
  public long countUnreadActivities(String username) {
    Identity userIdentity = getUserIdentity(username);
    List<String> spaceIds = spaceService.getMemberSpacesIds(username, 0, -1);
    if (CollectionUtils.isEmpty(spaceIds)) {
      return 0l;
    }
    return countUnreadActivityIds(spaceIds, userIdentity);
  }

  @Override
  public long countUnreadActivitiesBySpace(String username, long spaceId) throws IllegalAccessException, ObjectNotFoundException {
    Identity userIdentity = getUserIdentity(username);
    String spaceIdString = String.valueOf(spaceId);
    Space space = spaceService.getSpaceById(spaceIdString);
    if (space == null) {
      throw new ObjectNotFoundException(String.format(SPACE_NOT_FOUND, spaceId));
    }
    if (!spaceService.isSuperManager(username) && !spaceService.isMember(space, username)
        && !spaceService.isManager(space, username)) {
      throw new IllegalAccessException(String.format(USER_NOT_MEMBER_OF_SPACE, username, spaceId));
    }
    return countUnreadActivityIds(Collections.singletonList(spaceIdString), userIdentity);
  }

  private void mergeExistingUnreadProperties(SpaceWebNotificationItem notificationItem,
                                             MetadataKey metadataKey,
                                             MetadataObject metadataObject) throws ObjectNotFoundException {
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByMetadataAndObject(metadataKey, metadataObject);
    if (CollectionUtils.isNotEmpty(metadataItems)) {
      for (MetadataItem metadataItem : metadataItems) {
        appendNotificationSubItems(notificationItem, metadataItem);
        metadataService.deleteMetadataItem(metadataItem.getId(), false);
      }
    }
  }

  private void appendNotificationSubItems(SpaceWebNotificationItem notificationItem, MetadataItem metadataItem) {
    if (MapUtils.isNotEmpty(metadataItem.getProperties())) {
      String concatenatedSubItemIds = metadataItem.getProperties().get(APPLICATION_SUB_ITEM_IDS);
      if (StringUtils.isNotBlank(concatenatedSubItemIds)) {
        Arrays.stream(StringUtils.split(concatenatedSubItemIds, ",")).forEach(notificationItem::addApplicationSubItem);
      }
    }
  }

  private Map<String, String> toMetadataProperties(SpaceWebNotificationItem notificationItem) {
    Map<String, String> props = new HashMap<>();
    props.put(APPLICATION_SUB_ITEM_IDS, StringUtils.join(notificationItem.getApplicationSubItemIds(), ","));
    props.put(METADATA_ACTIVITY_ID_PROPERTY, notificationItem.getActivityId());
    return props;
  }

  private SpaceWebNotificationItem getSpaceWebNotificationItem(NotificationInfo notification, String username) {
    SpaceWebNotificationPlugin spaceWebNotification = plugins.stream()
                                                             .filter(plugin -> plugin.isManagedPlugin(notification.getKey()))
                                                             .findFirst()
                                                             .orElse(null);
    if (spaceWebNotification != null) {
      SpaceWebNotificationItem spaceApplicationItem = spaceWebNotification.getSpaceApplicationItem(notification, username);
      if (spaceApplicationItem != null
          && spaceApplicationItem.getSpaceId() > 0
          && spaceApplicationItem.getUserId() > 0
          && StringUtils.isNotBlank(spaceApplicationItem.getApplicationItemId())
          && StringUtils.isNotBlank(spaceApplicationItem.getApplicationName())) {
        return spaceApplicationItem;
      }
    }
    return null;
  }

  private List<Long> getUnreadActivityIds(long offset, long limit, List<String> spaceIds, Identity userIdentity) {
    List<MetadataItem> unreadItems =
                                   metadataService.getMetadataItemsByMetadataNameAndTypeAndSpaceIds(userIdentity.getId(),
                                                                                                    METADATA_TYPE_NAME,
                                                                                                    spaceIds.stream()
                                                                                                            .map(Long::parseLong)
                                                                                                            .toList(),
                                                                                                    offset,
                                                                                                    limit);
    if (CollectionUtils.isEmpty(unreadItems)) {
      return Collections.emptyList();
    }
    return unreadItems.stream()
                      .map(MetadataItem::getProperties)
                      .filter(Objects::nonNull)
                      .map(prop -> prop.get(METADATA_ACTIVITY_ID_PROPERTY))
                      .filter(Objects::nonNull)
                      .map(Long::parseLong)
                      .toList();
  }

  private long countUnreadActivityIds(List<String> spaceIds, Identity userIdentity) {
    return metadataService.countMetadataItemsByMetadataTypeAndSpacesIdAndCreatorId(METADATA_TYPE_NAME,
                                                                                   Long.parseLong(userIdentity.getId()),
                                                                                   spaceIds.stream()
                                                                                           .map(Long::parseLong)
                                                                                           .toList())
                          .values()
                          .stream()
                          .reduce(0l, Long::sum);
  }

  private Identity getUserIdentity(String username) {
    Identity userIdentity = identityManager.getOrCreateUserIdentity(username);
    if (userIdentity == null) {
      throw new IllegalArgumentException("User " + username + " doesn't exist");
    }
    return userIdentity;
  }

}
