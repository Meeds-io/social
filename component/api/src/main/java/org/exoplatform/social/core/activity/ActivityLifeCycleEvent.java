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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.activity;

import org.exoplatform.social.common.lifecycle.LifeCycleEvent;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

public class ActivityLifeCycleEvent extends LifeCycleEvent<ExoSocialActivity, String> {

  public enum Type {
    SAVE_ACTIVITY,
    UPDATE_ACTIVITY,
    SAVE_COMMENT,
    UPDATE_COMMENT,
    LIKE_ACTIVITY,
    LIKE_COMMENT,
    DELETE_LIKE_ACTIVITY,
    DELETE_LIKE_COMMENT,
    DELETE_COMMENT,
    DELETE_ACTIVITY,
    SHARED_ACTIVITY,
    HIDE_ACTIVITY,
    PIN_ACTIVITY,
    UNPIN_ACTIVITY,
  }

  private Type              type;

  private ExoSocialActivity activity;

  private String            userId;

  public ActivityLifeCycleEvent(Type type, ExoSocialActivity activity) {
    // temp set source as activityId
    super(activity, activity.getId());
    this.activity = activity;
    this.type = type;
  }

  public ActivityLifeCycleEvent(Type type, ExoSocialActivity activity, String userId) {
    // temp set source as activityId
    super(activity, activity.getId());
    this.activity = activity;
    this.type = type;
    this.userId = userId;
  }

  public Type getType() {
    return type;
  }

  public ExoSocialActivity getActivity() {
    return activity;
  }

  public String getActivityId() {
    return activity.getId();
  }

  public String getUserId() {
    return userId;
  }
}
