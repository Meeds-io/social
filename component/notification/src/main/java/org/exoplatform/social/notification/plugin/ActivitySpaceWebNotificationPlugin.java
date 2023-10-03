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
package org.exoplatform.social.notification.plugin;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;

public class ActivitySpaceWebNotificationPlugin extends SpaceWebNotificationPlugin {

  public static final String ID = "ActivitySpaceWebNotificationPlugin";

  private ActivityManager    activityManager;

  public ActivitySpaceWebNotificationPlugin(ActivityManager activityManager, IdentityManager identityManager, InitParams params) {
    super(identityManager, params);
    this.activityManager = activityManager;
  }

  @Override
  public SpaceWebNotificationItem getSpaceApplicationItem(NotificationInfo notification) {
    String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null) {
      return null;
    }

    MetadataObject metadataObject;
    if (activity.isComment()) {
      ExoSocialActivity parentActivity = activityManager.getActivity(activity.getParentId());
      metadataObject = parentActivity.getMetadataObject();
    } else {
      metadataObject = activity.getMetadataObject();
    }
    if (metadataObject != null && metadataObject.getSpaceId() > 0) {
      SpaceWebNotificationItem spaceWebNotificationItem = new SpaceWebNotificationItem(metadataObject.getType(),
                                                                                       metadataObject.getId(),
                                                                                       0,
                                                                                       metadataObject.getSpaceId());
      if (activity.isComment()) {
        spaceWebNotificationItem.setActivityId(activity.getParentId());
        spaceWebNotificationItem.addApplicationSubItem(activity.getId());
      } else {
        spaceWebNotificationItem.setActivityId(activity.getId());
      }
      return spaceWebNotificationItem;
    } else {
      return null;
    }
  }

}
