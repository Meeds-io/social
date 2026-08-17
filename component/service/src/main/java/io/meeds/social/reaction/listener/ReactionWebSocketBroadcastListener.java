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
package io.meeds.social.reaction.listener;

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

import io.meeds.social.reaction.model.Reaction;
import io.meeds.social.reaction.service.ReactionService;

import jakarta.annotation.PostConstruct;

/**
 * Glue listener (no business logic): the like websocket message fires before
 * the typed reaction item is written, so remote clients refetching on it can
 * cache a plain like; re-emitting the same stream modification after the
 * reaction write gives them a post-item signal to refresh with.
 */
@Component
public class ReactionWebSocketBroadcastListener extends Listener<Reaction, String> {

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
    listenerService.addListener(ReactionService.REACTION_CREATED_EVENT_NAME, this);
    listenerService.addListener(ReactionService.REACTION_UPDATED_EVENT_NAME, this);
  }

  @Override
  public void onEvent(Event<Reaction, String> event) throws Exception {
    Reaction reaction = event.getSource();
    ExoSocialActivity activity = activityManager.getActivity(reaction.getObjectId());
    if (activity == null || activity.isHidden()) {
      return;
    }
    String activityId = activity.isComment() ? activity.getParentId() : activity.getId();
    activityStreamWebSocketService.sendMessage(new ActivityStreamModification(activityId, "likeActivity", getSpaceId(activity)));
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
