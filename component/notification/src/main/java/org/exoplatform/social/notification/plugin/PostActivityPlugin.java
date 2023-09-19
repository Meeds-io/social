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

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.notification.Utils;

public class PostActivityPlugin extends BaseNotificationPlugin {

  public static final String ID = "PostActivityPlugin";

  public PostActivityPlugin(InitParams initParams) {
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
      String receiver = activity.getStreamOwner();
      if (Utils.getActivityManager().isNotificationEnabled(activity, receiver)) {
        String poster = Utils.getUserId(activity.getPosterId());
        return NotificationInfo.instance()
                               .to(receiver)
                               .setFrom(poster)
                               .with(SocialNotificationUtils.POSTER.getKey(), poster)
                               .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
                               .key(getId())
                               .end();
      }
    } catch (Exception e) {
      ctx.setException(e);
    }

    return null;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
    return Utils.getActivityManager().isNotificationEnabled(activity)
        && !activity.getStreamOwner().equals(Utils.getUserId(activity.getPosterId()))
        && !Utils.isSpaceActivity(activity);
  }

}
