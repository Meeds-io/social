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

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.attachment.AttachmentPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;

public class ActivityAttachmentPlugin extends AttachmentPlugin {

  public static final String    ACTIVITY_ATTACHMENT_TYPE = "activity";

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
    return activity != null && activityManager.isActivityViewable(activity, userIdentity);
  }

  @Override
  public boolean hasEditPermission(Identity userIdentity, String activityId) throws ObjectNotFoundException {
    ExoSocialActivity activity = getActivity(activityId);
    return activity != null && activityManager.isActivityEditable(activity, userIdentity);
  }

  @Override
  public long getAudienceId(String activityId) throws ObjectNotFoundException {
    ExoSocialActivity activity = getParentActivity(activityId);
    return activity == null || StringUtils.isBlank(activity.getStreamId()) ? 0 : Long.parseLong(activity.getStreamId());
  }

  @Override
  public long getSpaceId(String activityId) throws ObjectNotFoundException {
    ExoSocialActivity activity = getParentActivity(activityId);
    return activity == null || StringUtils.isBlank(activity.getSpaceId()) ? 0 : Long.parseLong(activity.getSpaceId());
  }

  private ExoSocialActivity getParentActivity(String activityId) throws ObjectNotFoundException {
    ExoSocialActivity activity = getActivity(activityId);
    if (activity != null && activity.isComment()) {
      activity = getActivity(activity.getParentId());
    }
    return activity;
  }

  private ExoSocialActivity getActivity(String activityId) throws ObjectNotFoundException {
    activityId = StringUtils.replace(activityId, "comment", "");
    if (StringUtils.isBlank(activityId)) {
      return null;
    }
    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null) {
      throw new ObjectNotFoundException("Activity with id " + activityId + " wasn't found");
    }
    return activity;
  }
}
