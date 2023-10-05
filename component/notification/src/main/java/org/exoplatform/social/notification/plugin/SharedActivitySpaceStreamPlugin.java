/**
 * Copyright (C) 2022 eXo Platform SAS.
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
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.notification.Utils;

public class SharedActivitySpaceStreamPlugin extends BaseNotificationPlugin {

  public static final String ID = "SharedActivitySpaceStreamPlugin";

  protected ActivityStorage activityStorage;

  public SharedActivitySpaceStreamPlugin(InitParams initParams , ActivityStorage activityStorage) {
    super(initParams);
    this.activityStorage = activityStorage;
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return true;
  }

  @Override
  protected NotificationInfo makeNotification(NotificationContext ctx) {
    try {
      ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
      String originalTitle = ctx.value(SocialNotificationUtils.ORIGINAL_TITLE);
      String titleShared = ctx.value(SocialNotificationUtils.ORIGINAL_TITLE_SHARED);
      Space space = Utils.getSpaceService().getSpaceByPrettyName(activity.getStreamOwner());
      String poster = Utils.getUserId(activity.getPosterId());

      return NotificationInfo.instance()
                             .key(getId())
                             .setSpaceId(Long.parseLong(space.getId()))
                             .with(SocialNotificationUtils.POSTER.getKey(), poster)
                             .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
                             .with(SocialNotificationUtils.SPACE_ID.getKey(), space.getId())
                             .with(SocialNotificationUtils.ORIGINAL_TITLE.getKey(), originalTitle)
                             .with(SocialNotificationUtils.ORIGINAL_TITLE_SHARED.getKey(),titleShared)
                             .setFrom(poster)
                             .to(Utils.getDestinataires(activity, space)).end();
    } catch (Exception e) {
      ctx.setException(e);
    }
    return null;
  }
}
