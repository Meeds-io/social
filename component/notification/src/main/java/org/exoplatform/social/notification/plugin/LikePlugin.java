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

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.Utils;

import java.util.ArrayList;
import java.util.List;

public class LikePlugin extends BaseNotificationPlugin {
  
  public LikePlugin(InitParams initParams) {
    super(initParams);
  }

  public static final String ID = "LikePlugin";
  
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
    boolean isMember = false;

    if (spaceId != null) {
      SpaceService spaceService = Utils.getSpaceService();
      Space space = spaceService.getSpaceById(spaceId);
      isMember = spaceService.isMember(space, likeTo);
    }

    if (spaceId != null && !isMember) {
      return null;
    }
    return NotificationInfo.instance()
                               .to(likeTo)
                               .with(SocialNotificationUtils.ACTIVITY_ID.getKey(), activity.getId())
                               .with(SocialNotificationUtils.LIKER.getKey(), liker)
                               .key(getId()).end();
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    ExoSocialActivity activity = ctx.value(SocialNotificationUtils.ACTIVITY);
    String[] likersId = activity.getLikeIdentityIds();
    if (activity.getPosterId().equals(likersId[likersId.length - 1])) {
      return false;
    }
    return true;
  }
}
