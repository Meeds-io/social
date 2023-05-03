/*
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
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.plugin;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.metadata.AttachmentPlugin;

public class ActivityAttachmentPlugin extends AttachmentPlugin {

  private static final String   ACTIVITY_ATTACHMENT_TYPE = "activity";

  private final ActivityManager activityManager;

  public ActivityAttachmentPlugin(ActivityManager activityManager) {
    this.activityManager = activityManager;
  }

  @Override
  public String getObjectType() {
    return ACTIVITY_ATTACHMENT_TYPE;
  }

  @Override
  public boolean hasAccessPermission(Identity userIdentity, String activityId) throws ObjectNotFoundException {
    ExoSocialActivity activity = getActivity(activityId);
    return activityManager.isActivityViewable(activity, userIdentity);
  }

  @Override
  public boolean hasEditPermission(Identity userIdentity, String activityId) throws ObjectNotFoundException {
    ExoSocialActivity activity = getActivity(activityId);
    return activityManager.isActivityEditable(activity, userIdentity);
  }

  private ExoSocialActivity getActivity(String activityId) throws ObjectNotFoundException {
    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null) {
      throw new ObjectNotFoundException("Activity with id " + activityId + " wasn't found");
    }
    return activity;
  }
}
