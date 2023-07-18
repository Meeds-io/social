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
package org.exoplatform.social.core.activity;

import org.exoplatform.social.common.lifecycle.AbstractLifeCycle;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent.Type;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;


public class ActivityLifeCycle extends AbstractLifeCycle<ActivityListener, ActivityLifeCycleEvent> {

  @Override
  protected void dispatchEvent(ActivityListener listener, ActivityLifeCycleEvent event) {
    switch(event.getType()) {
      case SAVE_ACTIVITY:
        listener.saveActivity(event);
        break;
      case UPDATE_ACTIVITY:
        listener.updateActivity(event);
        break;
      case SAVE_COMMENT: 
        listener.saveComment(event);
        break;
      case UPDATE_COMMENT:
        listener.updateComment(event);
        break;
      case LIKE_ACTIVITY:
        listener.likeActivity(event);
        break;
      case DELETE_LIKE_ACTIVITY:
        listener.deleteLikeActivity(event);
        break;
      case LIKE_COMMENT:
        listener.likeComment(event);
        break;
      case DELETE_LIKE_COMMENT:
        listener.deleteLikeComment(event);
        break;
      case DELETE_COMMENT:
        listener.deleteComment(event);
        break;
      case DELETE_ACTIVITY:
        listener.deleteActivity(event);
        break;
      case SHARED_ACTIVITY:
        listener.shareActivity(event);
        break;
      case HIDE_ACTIVITY:
        listener.hideActivity(event);
        break;
      case PIN_ACTIVITY:
        listener.pinActivity(event);
        break;
      case UNPIN_ACTIVITY:
        listener.unpinActivity(event);
        break;
    }
  }
  
  public void saveActivity(ExoSocialActivity activity) {
    broadcast(new ActivityLifeCycleEvent(Type.SAVE_ACTIVITY, activity));
  }

  public void updateActivity(ExoSocialActivity activity) {
    broadcast(new ActivityLifeCycleEvent(Type.UPDATE_ACTIVITY, activity));
  }

  public void saveComment(ExoSocialActivity activity) {
    broadcast(new ActivityLifeCycleEvent(Type.SAVE_COMMENT, activity));
  }

  public void updateComment(ExoSocialActivity activity) {
    broadcast(new ActivityLifeCycleEvent(Type.UPDATE_COMMENT, activity));
  }
  
  public void likeActivity(ExoSocialActivity activity, String userIdentityId) {
    broadcast(new ActivityLifeCycleEvent(Type.LIKE_ACTIVITY, activity, userIdentityId));
  }

  public void likeComment(ExoSocialActivity activity, String userIdentityId) {
    broadcast(new ActivityLifeCycleEvent(Type.LIKE_COMMENT, activity, userIdentityId));
  }

  public void deleteLikeActivity(ExoSocialActivity activity, String userIdentityId) {
    broadcast(new ActivityLifeCycleEvent(Type.DELETE_LIKE_ACTIVITY, activity, userIdentityId));
  }

  public void deleteLikeComment(ExoSocialActivity activity, String userIdentityId) {
    broadcast(new ActivityLifeCycleEvent(Type.DELETE_LIKE_COMMENT, activity, userIdentityId));
  }

  public void deleteActivity(ExoSocialActivity activity) {
    broadcast(new ActivityLifeCycleEvent(Type.DELETE_ACTIVITY, activity));
  }

  public void deleteComment(ExoSocialActivity activity) {
    broadcast(new ActivityLifeCycleEvent(Type.DELETE_COMMENT, activity));
  }

  public void shareActivity(ExoSocialActivity activity) {
    broadcast(new ActivityLifeCycleEvent(Type.SHARED_ACTIVITY, activity));
  }

  public void hideActivity(ExoSocialActivity activity) {
    broadcast(new ActivityLifeCycleEvent(Type.HIDE_ACTIVITY, activity));
  }

  public void pinActivity(ExoSocialActivity activity, String userIdentityId) {
    broadcast(new ActivityLifeCycleEvent(Type.PIN_ACTIVITY, activity, userIdentityId));
  }

  public void unpinActivity(ExoSocialActivity activity) {
    broadcast(new ActivityLifeCycleEvent(Type.UNPIN_ACTIVITY, activity));
  }

}
