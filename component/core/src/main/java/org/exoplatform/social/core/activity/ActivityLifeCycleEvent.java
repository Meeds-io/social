/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
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
package org.exoplatform.social.core.activity;

import org.exoplatform.social.common.lifecycle.LifeCycleEvent;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

public class ActivityLifeCycleEvent extends LifeCycleEvent<ExoSocialActivity, String> {
  
  public enum Type {SAVE_ACTIVITY, UPDATE_ACTIVITY, SAVE_COMMENT, UPDATE_COMMENT, LIKE_ACTIVITY, LIKE_COMMENT, DELETE_COMMENT, DELETE_ACTIVITY}
  
  private Type type;
  private ExoSocialActivity activity;
  private String            activityId;

  public ActivityLifeCycleEvent(Type type, ExoSocialActivity activity) {
    // temp set source as activityId
    super(activity, activity.getId());
    this.activity = activity;
    this.type = type;
  }
  
  public Type getType() {
    return type;
  }

  public ExoSocialActivity getActivity() {
    return activity;
  }

  public String getActivityId() {
    return activityId;
  }
}
