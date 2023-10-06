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
package org.exoplatform.social.websocket.listener;

import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.websocket.ActivityStreamWebSocketService;
import org.exoplatform.social.websocket.entity.ActivityStreamModification;

/**
 * An activity listener that will broadcast changes to connected users. This
 * will allow to have dynamic UI and fresh updates without refreshing page.
 */
public class ActivityStreamUpdateBroadcast extends ActivityListenerPlugin {

  private ActivityStreamWebSocketService activityStreamWebSocketService;

  private ActivityManager                activityManager;

  private SpaceService                   spaceService;

  public ActivityStreamUpdateBroadcast(ActivityStreamWebSocketService activityStreamWebSocketService,
                                       ActivityManager activityManager,
                                       SpaceService spaceService) {
    this.activityStreamWebSocketService = activityStreamWebSocketService;
    this.activityManager = activityManager;
    this.spaceService = spaceService;
  }

  @Override
  public void saveActivity(ActivityLifeCycleEvent event) {
    if (event.getActivity() == null || event.getActivity().isHidden()) {
      return;
    }
    String activityId = getActivityId(event);
    ActivityStreamModification activityStreamModification = new ActivityStreamModification(activityId,  "createActivity",getSpaceId(event));
    activityStreamWebSocketService.sendMessage(activityStreamModification);
  }

  @Override
  public void updateActivity(ActivityLifeCycleEvent event) {
    if (event.getActivity() == null || event.getActivity().isHidden()) {
      return;
    }
    String activityId = getActivityId(event);
    ActivityStreamModification activityStreamModification = new ActivityStreamModification(activityId, "updateActivity", getSpaceId(event));
    activityStreamWebSocketService.sendMessage(activityStreamModification);
  }

  @Override
  public void pinActivity(ActivityLifeCycleEvent event) {
    if (event.getActivity() == null || event.getActivity().isHidden()) {
      return;
    }
    String activityId = getActivityId(event);
    ActivityStreamModification activityStreamModification = new ActivityStreamModification(activityId, "pinActivity", getSpaceId(event));
    activityStreamWebSocketService.sendMessage(activityStreamModification);
  }

  @Override
  public void deleteActivity(ActivityLifeCycleEvent event) {
    if (event.getActivity() == null || event.getActivity().isHidden()) {
      return;
    }
    String activityId = getActivityId(event);
    ActivityStreamModification activityStreamModification = new ActivityStreamModification(activityId, "deleteActivity", getSpaceId(event));
    activityStreamWebSocketService.sendMessage(activityStreamModification);
  }

  @Override
  public void saveComment(ActivityLifeCycleEvent event) {
    if (event.getActivity() == null || event.getActivity().isHidden()) {
      return;
    }
    String activityId = getActivityId(event);
    String commentId = getCommentId(event);
    String parentCommentId = getParentCommentId(event);
    ActivityStreamModification activityStreamModification = new ActivityStreamModification(activityId, commentId, parentCommentId, "createComment", getSpaceId(activityId));
    activityStreamWebSocketService.sendMessage(activityStreamModification);
  }

  @Override
  public void deleteComment(ActivityLifeCycleEvent event) {
    if (event.getActivity() == null || event.getActivity().isHidden()) {
      return;
    }
    String activityId = getActivityId(event);
    String commentId = getCommentId(event);
    String parentCommentId = getParentCommentId(event);
    ActivityStreamModification activityStreamModification = new ActivityStreamModification(activityId, commentId, parentCommentId, "deleteComment", getSpaceId(activityId));
    activityStreamWebSocketService.sendMessage(activityStreamModification);
  }

  @Override
  public void updateComment(ActivityLifeCycleEvent event) {
    if (event.getActivity() == null || event.getActivity().isHidden()) {
      return;
    }
    String activityId = getActivityId(event);
    String commentId = getCommentId(event);
    String parentCommentId = getParentCommentId(event);
    ActivityStreamModification activityStreamModification = new ActivityStreamModification(activityId, commentId, parentCommentId, "updateComment", getSpaceId(activityId));
    activityStreamWebSocketService.sendMessage(activityStreamModification);
  }

  @Override
  public void likeActivity(ActivityLifeCycleEvent event) {
    if (event.getActivity() == null || event.getActivity().isHidden()) {
      return;
    }
    String activityId = getActivityId(event);
    ActivityStreamModification activityStreamModification = new ActivityStreamModification(activityId, "likeActivity", getSpaceId(event));
    activityStreamWebSocketService.sendMessage(activityStreamModification);
  }

  @Override
  public void likeComment(ActivityLifeCycleEvent event) {
    if (event.getActivity() == null || event.getActivity().isHidden()) {
      return;
    }
    String activityId = getActivityId(event);
    String commentId = getCommentId(event);
    String parentCommentId = getParentCommentId(event);
    ActivityStreamModification activityStreamModification = new ActivityStreamModification(activityId, commentId, parentCommentId, "likeComment", getSpaceId(activityId));
    activityStreamWebSocketService.sendMessage(activityStreamModification);
  }

  private String getActivityId(ActivityLifeCycleEvent event) {
    return event.getActivity().isComment() ? event.getActivity().getParentId() : event.getActivity().getId();
  }

  private String getSpaceId(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getActivity();
    return getSpaceId(activity);
  }

  private String getSpaceId(String activityId) {
    return getSpaceId(activityManager.getActivity(activityId));
  }

  private String getSpaceId(ExoSocialActivity activity) {
    if (activity != null && activity.getActivityStream().getType() == ActivityStream.Type.SPACE) {
      Space space = spaceService.getSpaceByPrettyName(activity.getActivityStream().getPrettyId());
      if (space != null) {
        return space.getId();
      }
    }
    return null;
  }

  private String getCommentId(ActivityLifeCycleEvent event) {
    return event.getActivity().getParentId() == null ? null : event.getActivity().getId();
  }

  private String getParentCommentId(ActivityLifeCycleEvent event) {
    return event.getActivity().getParentCommentId();
  }
}
