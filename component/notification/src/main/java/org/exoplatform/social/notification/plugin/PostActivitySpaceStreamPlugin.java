/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.social.notification.plugin;

import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.Utils;

public class PostActivitySpaceStreamPlugin extends BaseNotificationPlugin {

  public static final String ID = "PostActivitySpaceStreamPlugin";

  public PostActivitySpaceStreamPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    try {
      ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
      String originalTitle = ctx.value(SocialNotificationUtils.ORIGINAL_TITLE);
      Space space = Utils.getSpaceService().getSpaceByPrettyName(activity.getStreamOwner());
      String poster = Utils.getUserId(activity.getPosterId());

      List<String> receivers = Utils.getDestinataires(activity, space)
                                    .stream()
                                    .filter(username -> Utils.getActivityManager().isNotificationEnabled(activity, username))
                                    .toList();
      return NotificationInfo.instance()
                             .key(getId())
                             .setSpaceId(Long.parseLong(space.getId()))
                             .with(SocialNotificationUtils.POSTER.getKey(), poster)
                             .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
                             .with(SocialNotificationUtils.ORIGINAL_TITLE.getKey(), originalTitle)
                             .to(receivers)
                             .setFrom(poster)
                             .end();
    } catch (Exception e) {
      ctx.setException(e);
    }

    return null;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
    if (!Utils.getActivityManager().isNotificationEnabled(activity)) {
      return false;
    }

    Identity spaceIdentity = Utils.getIdentityManager().getOrCreateSpaceIdentity(activity.getStreamOwner());
    // if the space is not null and it's not the default activity of space, then
    // it's valid to make notification
    return spaceIdentity != null && !activity.getPosterId().equals(spaceIdentity.getId());
  }
}
