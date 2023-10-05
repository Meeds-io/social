/**
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 * 
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
package org.exoplatform.social.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.Utils;

/**
 * Notification plugin for Comments Likes
 */
public class LikeCommentPlugin extends BaseNotificationPlugin {

  public LikeCommentPlugin(InitParams initParams) {
    super(initParams);
  }

  public static final String ID = "LikeCommentPlugin";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);

    String[] likersId = activity.getLikeIdentityIds();
    String liker = Utils.getUserId(likersId[likersId.length - 1]);

    String likeTo = Utils.getUserId(activity.getPosterId());
    String spaceId = !activity.isComment() ? activity.getSpaceId()
                                           : Utils.getActivityManager().getParentActivity(activity).getSpaceId();
    SpaceService spaceService = Utils.getSpaceService();
    if (spaceId != null && !spaceService.isSuperManager(likeTo)) {
      Space space = spaceService.getSpaceById(spaceId);
      if (!spaceService.isMember(space, likeTo)) {
        return null;
      }
    }
    NotificationInfo notification = NotificationInfo.instance()
                                                    .to(likeTo)
                                                    .setFrom(liker)
                                                    .setSpaceId(spaceId == null ? 0 : Long.parseLong(spaceId))
                                                    .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
                                                    .with(SocialNotificationUtils.LIKER.getKey(), liker)
                                                    .key(getId())
                                                    .end();
    notification = SocialNotificationUtils.addUserToPreviousNotification(notification,
                                                                         SocialNotificationUtils.LIKERS.getKey(),
                                                                         activity.getId(),
                                                                         liker);
    return notification;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
    String[] likersId = activity.getLikeIdentityIds();
    return !activity.getPosterId().equals(likersId[likersId.length - 1]);
  }
}
