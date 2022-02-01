/*
* Copyright (C) 2003-2021 eXo Platform SAS.
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
*/
package org.exoplatform.social.websocket.listenr;

import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
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

  private SpaceService spaceService;

  public ActivityStreamUpdateBroadcast(ActivityStreamWebSocketService activityStreamWebSocketService, SpaceService spaceService) {
    this.activityStreamWebSocketService = activityStreamWebSocketService;
    this.spaceService = spaceService;
  }

  @Override
  public void saveActivity(ActivityLifeCycleEvent event) {
    String activityId = getActivityId(event);
    ActivityStreamModification activityStreamModification = new ActivityStreamModification(activityId,  "createActivity",getSpaceId(event));
    activityStreamWebSocketService.sendMessage(activityStreamModification);
  }

  @Override
  public void updateActivity(ActivityLifeCycleEvent event) {
    String activityId = getActivityId(event);
    ActivityStreamModification activityStreamModification = new ActivityStreamModification(activityId, "updateActivity", getSpaceId(event));
    activityStreamWebSocketService.sendMessage(activityStreamModification);
  }

  @Override
  public void saveComment(ActivityLifeCycleEvent event) {
    String activityId = getActivityId(event);
    String commentId = getCommentId(event);
    String parentCommentId = getParentCommentId(event);
    ActivityStreamModification activityStreamModification = new ActivityStreamModification(activityId, commentId, parentCommentId, "createComment", getSpaceId(event));
    activityStreamWebSocketService.sendMessage(activityStreamModification);
  }

  @Override
  public void updateComment(ActivityLifeCycleEvent event) {
    String activityId = getActivityId(event);
    String commentId = getCommentId(event);
    String parentCommentId = getParentCommentId(event);
    ActivityStreamModification activityStreamModification = new ActivityStreamModification(activityId, commentId, parentCommentId, "updateComment", getSpaceId(event));
    activityStreamWebSocketService.sendMessage(activityStreamModification);
  }

  @Override
  public void likeActivity(ActivityLifeCycleEvent event) {
    String activityId = getActivityId(event);
    ActivityStreamModification activityStreamModification = new ActivityStreamModification(activityId, "likeActivity", getSpaceId(event));
    activityStreamWebSocketService.sendMessage(activityStreamModification);
  }

  @Override
  public void likeComment(ActivityLifeCycleEvent event) {
    String activityId = getActivityId(event);
    String commentId = getCommentId(event);
    String parentCommentId = getParentCommentId(event);
    ActivityStreamModification activityStreamModification = new ActivityStreamModification(activityId, commentId, parentCommentId, "likeComment", getSpaceId(event));
    activityStreamWebSocketService.sendMessage(activityStreamModification);
  }

  private String getActivityId(ActivityLifeCycleEvent event) {
    return event.getActivity().isComment() ? event.getActivity().getParentId() : event.getActivity().getId();
  }

  private String getSpaceId(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getActivity();
    String spaceId = null;
    if(activity.getActivityStream().getType() == ActivityStream.Type.SPACE) {
      Space space = spaceService.getSpaceByPrettyName(activity.getActivityStream().getPrettyId());
      if (space != null) {
        spaceId = space.getId();
      }
    }
    return spaceId;
  }

  private String getCommentId(ActivityLifeCycleEvent event) {
    return event.getActivity().getParentId() == null ? null : event.getActivity().getId();
  }

  private String getParentCommentId(ActivityLifeCycleEvent event) {
    return event.getActivity().getParentCommentId();
  }
}
