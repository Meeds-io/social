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

package io.meeds.social.notification.rest.utils;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.entity.ProfileEntity;
import org.exoplatform.social.rest.entity.SpaceEntity;

import io.meeds.social.notification.rest.model.WebNotificationListRestEntity;
import io.meeds.social.notification.rest.model.WebNotificationRestEntity;

public class WebNotificationRestEntityBuilder {

  private WebNotificationRestEntityBuilder() {
    // Utils class
  }

  public static WebNotificationRestEntity toRestEntity(WebNotificationService webNotificationService,
                                                       IdentityManager identityManager,
                                                       SpaceService spaceService,
                                                       NotificationInfo notification,
                                                       boolean isOnPopover) {
    Identity identity = getSender(identityManager, notification);
    Space space = getSpace(spaceService, notification);
    ProfileEntity profileEntity = identity == null ? null : EntityBuilder.buildEntityProfile(identity.getProfile(), null);
    SpaceEntity spaceEntity = space == null ? null : EntityBuilder.buildEntityFromSpace(space, null, null, null);

    return new WebNotificationRestEntity(notification.getId(),
                                         notification.getTitle(),
                                         notification.getKey().getId(),
                                         profileEntity,
                                         spaceEntity,
                                         webNotificationService.getNotificationMessage(notification, isOnPopover),
                                         notification.getOwnerParameter(),
                                         notification.isRead(),
                                         notification.isMutable(),
                                         notification.isSpaceMuted(),
                                         notification.getLastModifiedDate());
  }

  public static WebNotificationListRestEntity toRestEntity(WebNotificationService webNftService, // NOSONAR
                                                           IdentityManager identityManager,
                                                           SpaceService spaceService,
                                                           List<NotificationInfo> notificationInfos,
                                                           boolean isOnPopover,
                                                           int badge,
                                                           Map<String, Integer> badgesByPlugin,
                                                           int offset,
                                                           int limit) {
    return new WebNotificationListRestEntity(notificationInfos.stream()
                                                              .map(notification -> toRestEntity(webNftService,
                                                                                                identityManager,
                                                                                                spaceService,
                                                                                                notification,
                                                                                                isOnPopover))
                                                              .toList(),
                                             badge,
                                             badgesByPlugin,
                                             offset,
                                             limit);
  }

  private static Identity getSender(IdentityManager identityManager, NotificationInfo notification) {
    String username = notification.getFrom();
    if (StringUtils.isBlank(username)) {
      username = notification.getOwnerParameter().get("poster");
    }
    if (StringUtils.isBlank(username)) {
      username = notification.getOwnerParameter().get("username");
    }
    if (StringUtils.isBlank(username)) {
      username = notification.getOwnerParameter().get("profile");
    }
    if (StringUtils.isBlank(username)) {
      username = notification.getOwnerParameter().get("sender");
    }
    if (StringUtils.isBlank(username)) {
      username = notification.getOwnerParameter().get("modifier");
    }
    if (StringUtils.isBlank(username)) {
      username = notification.getOwnerParameter().get("MODIFIER_ID");
    }
    if (StringUtils.isBlank(username)) {
      username = notification.getOwnerParameter().get("SENDER_ID");
    }
    if (StringUtils.isBlank(username)) {
      username = notification.getOwnerParameter().get("request_from");
    }
    if (StringUtils.isBlank(username)) {
      username = notification.getOwnerParameter().get("likersId");
    }
    if (StringUtils.isBlank(username)) {
      username = notification.getOwnerParameter().get("creator");
    }
    if (StringUtils.isBlank(username)) {
      username = notification.getOwnerParameter().get("creatorId");
    }
    if (StringUtils.isBlank(username)) {
      return null;
    } else {
      Identity identity = identityManager.getOrCreateUserIdentity(username);
      if (identity != null) {
        return identity;
      }
    }
    if (StringUtils.isNumeric(username)) {
      return identityManager.getIdentity(username);
    } else {
      return null;
    }
  }

  private static Space getSpace(SpaceService spaceService, NotificationInfo notification) {
    String spaceId = notification.getOwnerParameter().get("spaceId");
    if (StringUtils.isNotBlank(spaceId)) {
      return spaceService.getSpaceById(spaceId);
    }
    String spacePrettyName = notification.getOwnerParameter().get("prettyName");
    if (StringUtils.isNotBlank(spacePrettyName)) {
      return spaceService.getSpaceByPrettyName(spacePrettyName);
    }
    String activityId = notification.getOwnerParameter().get("activityId");
    if (StringUtils.isNotBlank(activityId) && StringUtils.isNumeric(activityId)) {
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      if (activity != null && activity.getActivityStream() != null && activity.getActivityStream().isSpace()) {
        spaceId = activity.getSpaceId();
        if (StringUtils.isNotBlank(spaceId)) {
          return spaceService.getSpaceById(spaceId);
        }
      }
    }
    return null;
  }

}
