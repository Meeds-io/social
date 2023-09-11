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

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.entity.ProfileEntity;

import io.meeds.social.notification.rest.model.WebNotificationListRestEntity;
import io.meeds.social.notification.rest.model.WebNotificationRestEntity;

public class WebNotificationRestEntityBuilder {

  private WebNotificationRestEntityBuilder() {
    // Utils class
  }

  public static WebNotificationRestEntity toRestEntity(WebNotificationService webNotificationService,
                                                       IdentityManager identityManager,
                                                       NotificationInfo notification,
                                                       boolean isOnPopover) {
    String username = notification.getFrom();
    Identity identity = identityManager.getOrCreateUserIdentity(username);
    ProfileEntity profileEntity = identity == null ? null : EntityBuilder.buildEntityProfile(identity.getProfile(), username);

    return new WebNotificationRestEntity(notification.getId(),
                                         notification.getTitle(),
                                         notification.getKey().getId(),
                                         profileEntity,
                                         webNotificationService.getNotificationMessage(notification, isOnPopover),
                                         notification.getOwnerParameter(),
                                         notification.isRead(),
                                         notification.getLastModifiedDate());
  }

  public static WebNotificationListRestEntity toRestEntity(WebNotificationService webNftService,
                                                           IdentityManager identityManager,
                                                           List<NotificationInfo> notificationInfos,
                                                           boolean isOnPopover,
                                                           int offset,
                                                           int limit,
                                                           int badge) {
    return new WebNotificationListRestEntity(notificationInfos.stream()
                                                              .map(notification -> toRestEntity(webNftService,
                                                                                                identityManager,
                                                                                                notification,
                                                                                                isOnPopover))
                                                              .toList(),
                                             badge,
                                             offset,
                                             limit);
  }

}
