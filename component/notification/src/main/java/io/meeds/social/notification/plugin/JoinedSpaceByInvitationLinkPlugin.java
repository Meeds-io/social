/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package io.meeds.social.notification.plugin;

import io.meeds.social.notification.util.NotificationUtils;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;

public class JoinedSpaceByInvitationLinkPlugin extends BaseNotificationPlugin {

  public static final String ID  = "JoinedSpaceByInvitationLinkPlugin";

  public JoinedSpaceByInvitationLinkPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isValid(NotificationContext notificationContext) {
    return true;
  }

  @Override
  protected NotificationInfo makeNotification(NotificationContext notificationContext) {
    String invitedUserDisplayName = notificationContext.value(NotificationUtils.INVITED_USER);
    String invitedUserId = notificationContext.value(NotificationUtils.INVITED_USER_ID);
    String inviterId = notificationContext.value(NotificationUtils.INVITER_ID);
    String spaceId = notificationContext.value(NotificationUtils.SPACE_ID);
    String spaceAvatarUrl = notificationContext.value(NotificationUtils.SPACE_AVATAR_URL);
    String spaceDisplayName = notificationContext.value(NotificationUtils.SPACE_DISPLAY_NAME);

    return NotificationInfo.instance()
                           .setFrom(invitedUserId)
                           .to(inviterId)
                           .with(NotificationUtils.INVITED_USER.getKey(), invitedUserDisplayName)
                           .with(NotificationUtils.SPACE_AVATAR_URL.getKey(),spaceAvatarUrl)
                           .with(NotificationUtils.SPACE_ID.getKey(), spaceId)
                           .with(NotificationUtils.INVITER_ID.getKey(), inviterId)
                           .with(NotificationUtils.SPACE_DISPLAY_NAME.getKey(), spaceDisplayName)
                           .key(getKey())
                           .end();
  }
}
