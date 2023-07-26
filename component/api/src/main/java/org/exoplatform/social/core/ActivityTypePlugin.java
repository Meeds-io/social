/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

import lombok.Getter;

/**
 * A plugin to allow extending A new Activity Type behavior such as enabling
 * activity/comment type notification.
 */
public class ActivityTypePlugin extends BaseComponentPlugin {

  public static final String ENABLE_NOTIFICATION_PARAM = "enableNotification";

  public static final String ACTIVITY_TYPE_PARAM       = "type";

  @Getter
  protected String           activityType;

  @Getter
  protected boolean          enableNotification;

  public ActivityTypePlugin(InitParams params) {
    this.activityType = getParamValue(params, ACTIVITY_TYPE_PARAM, null);
    this.enableNotification = Boolean.parseBoolean(getParamValue(params, ENABLE_NOTIFICATION_PARAM, "true"));
  }

  /**
   * Return whether an activity is deletable or not
   * 
   * @param  activity        {@link ExoSocialActivity}
   * @param  userAclIdentity user {@link Identity} making the change
   * @return                 true is user can delete activity, else false
   */
  public boolean isActivityDeletable(ExoSocialActivity activity, Identity userAclIdentity) {
    throw new UnsupportedOperationException();
  }

  /**
   * Return whether an activity is editable or not
   * 
   * @param  activity        {@link ExoSocialActivity}
   * @param  userAclIdentity user {@link Identity} making the change
   * @return                 true is user can edit the activity, else false
   */
  public boolean isActivityEditable(ExoSocialActivity activity, Identity userAclIdentity) {
    throw new UnsupportedOperationException();
  }

  /**
   * Makes a check whether the activity notification is enabled for a given user
   * or not.
   * 
   * @param  activity {@link ExoSocialActivity}
   * @param  username {@link org.exoplatform.social.core.identity.model.Identity}
   *                    remote id
   * @return          true if enabled, else false
   */
  public boolean isEnableNotification(ExoSocialActivity activity, String username) {
    return isEnableNotification();
  }

  /**
   * Return whether an activity is viewable by a user or not
   * 
   * @param  activity        {@link ExoSocialActivity}
   * @param  userAclIdentity user {@link Identity} making the change
   * @return                 true is user can view the activity, else false
   */
  public boolean isActivityViewable(ExoSocialActivity activity, Identity userAclIdentity) {
    throw new UnsupportedOperationException();
  }

  /**
   * Return specific activity title
   * 
   * @param  activity {@link ExoSocialActivity}
   * @return          activity title to use in notification by example
   */
  public String getActivityTitle(ExoSocialActivity activity) {
    return activity.getTitle();
  }

  private String getParamValue(InitParams params, String paramName, String defaultValue) {
    return params == null || !params.containsKey(paramName) ? defaultValue : params.getValueParam(paramName).getValue();
  }

}
