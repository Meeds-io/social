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
package org.exoplatform.social.core.plugin;

import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.metadata.FavoriteACLPlugin;

public class ActivityFavoriteACLPlugin extends FavoriteACLPlugin {

  private static final String   ACTIVITY_FAVORITE_TYPE = "activity";

  private final ActivityManager activityManager;

  public ActivityFavoriteACLPlugin(ActivityManager activityManager) {
    this.activityManager = activityManager;
  }

  @Override
  public String getEntityType() {
    return ACTIVITY_FAVORITE_TYPE;
  }

  @Override
  public boolean canCreateFavorite(Identity userIdentity, String objectId) {
    ExoSocialActivity activity = activityManager.getActivity(String.valueOf(objectId));
    if (activity == null) {
      throw new IllegalStateException("Activity with id " + objectId + " wasn't found");
    }
    return activityManager.isActivityViewable(activity, userIdentity);
  }
}
