/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.report.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.websocket.ActivityStreamWebSocketService;
import org.exoplatform.social.websocket.entity.ActivityStreamModification;

import io.meeds.social.report.model.ActivityReport;
import io.meeds.social.report.service.ActivityReportService;

import jakarta.annotation.PostConstruct;

/**
 * Glue listener (no business logic): once a report is recorded, re-emits the
 * activity stream websocket modification so every open view of the reported
 * activity — the reporter's other tabs included — refetches it and derives the
 * fresh per-caller report flags. No report data travels in the message.
 */
@Component
public class ReportWebSocketBroadcastListener extends Listener<ActivityReport, Long> {

  @Autowired
  private ListenerService                listenerService;

  @Autowired
  private ActivityManager                activityManager;

  @Autowired
  private SpaceService                   spaceService;

  @Autowired
  private ActivityStreamWebSocketService activityStreamWebSocketService;

  @PostConstruct
  public void init() {
    listenerService.addListener(ActivityReportService.EVENT_ACTIVITY_REPORTED, this);
  }

  @Override
  public void onEvent(Event<ActivityReport, Long> event) throws Exception {
    ActivityReport report = event.getSource();
    ExoSocialActivity activity = activityManager.getActivity(report.getActivityId());
    if (activity == null || activity.isHidden()) {
      return;
    }
    ActivityStreamModification streamModification;
    if (activity.isComment()) {
      ExoSocialActivity parentActivity = activityManager.getActivity(activity.getParentId());
      streamModification = new ActivityStreamModification(activity.getParentId(),
                                                          "updateComment",
                                                          parentActivity == null ? null : getSpaceId(parentActivity));
      streamModification.setCommentId(activity.getId());
    } else {
      streamModification = new ActivityStreamModification(activity.getId(), "updateActivity", getSpaceId(activity));
    }
    activityStreamWebSocketService.sendMessage(streamModification);
  }

  private String getSpaceId(ExoSocialActivity activity) {
    if (activity.getActivityStream() != null && activity.getActivityStream().getType() == ActivityStream.Type.SPACE) {
      Space space = spaceService.getSpaceByPrettyName(activity.getActivityStream().getPrettyId());
      if (space != null) {
        return space.getId();
      }
    }
    return null;
  }

}
