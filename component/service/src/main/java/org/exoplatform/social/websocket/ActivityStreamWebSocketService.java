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
package org.exoplatform.social.websocket;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.CollectionUtils;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;
import org.picocontainer.Startable;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ActivityStream.Type;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.*;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.websocket.entity.ActivityStreamModification;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;

/**
 * A websocket service that will send information about modified Activity. This
 * will allow to have dynamic UI and fresh updates without refreshing page.
 */
public class ActivityStreamWebSocketService implements Startable {

  public static final String    COMETD_CHANNEL = "/eXo/Application/ActivityStream";

  private static final Log      LOG            = ExoLogger.getLogger(ActivityStreamWebSocketService.class);

  private PortalContainer       portalContainer;

  private ContinuationService   continuationService;

  private EXoContinuationBayeux continuationBayeux;

  private ActivityManager       activityManager;

  private RelationshipManager   relationshipManager;

  private IdentityManager       identityManager;

  private SpaceService          spaceService;

  private ExecutorService       executorService;

  public ActivityStreamWebSocketService(PortalContainer portalContainer,
                                        ActivityManager activityManager,
                                        IdentityManager identityManager,
                                        RelationshipManager relationshipManager,
                                        SpaceService spaceService,
                                        EXoContinuationBayeux continuationBayeux,
                                        ContinuationService continuationService) {
    this.portalContainer = portalContainer;
    this.continuationService = continuationService;
    this.continuationBayeux = continuationBayeux;
    this.activityManager = activityManager;
    this.relationshipManager = relationshipManager;
    this.spaceService = spaceService;
    this.identityManager = identityManager;
  }

  @Override
  public void start() {
    executorService = Executors.newFixedThreadPool(1);
  }

  @Override
  public void stop() {
    executorService.shutdownNow();
  }

  /**
   * Propagate an ActivityStream modification from Backend to frontend to add
   * dynamism in pages
   * 
   * @param activityStreamModification The ActivityStream modification object
   */
  public void sendMessage(ActivityStreamModification activityStreamModification) {
    String message = activityStreamModification.toString();
    String activityId = activityStreamModification.getActivityId();
    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null || activity.getActivityStream() == null) {
      continuationService.sendBroadcastMessage(COMETD_CHANNEL, message);
    } else if (activity.getActivityStream().getType() == Type.USER) {
      executorService.execute(() -> {
        RequestLifeCycle.begin(portalContainer);
        ExoContainerContext.setCurrentContainer(portalContainer);
        try {
          // Send to stream owner
          String userId = activity.getActivityStream().getPrettyId();
          continuationService.sendMessage(userId, COMETD_CHANNEL, message);

          // Send to stream owner connected users
          Identity identity = this.identityManager.getOrCreateUserIdentity(userId);
          Set<String> connectedUserIds = new HashSet<>(continuationBayeux.getConnectedUserIds());
          connectedUserIds.remove(userId);
          connectedUserIds.forEach(connectedUserId -> {
            Identity connectedIdentity = this.identityManager.getOrCreateUserIdentity(connectedUserId);
            if (relationshipManager.getStatus(identity, connectedIdentity) == Relationship.Type.CONFIRMED) {
              try {
                continuationService.sendMessage(connectedUserId, COMETD_CHANNEL, message);
              } catch (Exception e) {
                LOG.warn("Error sending WebSocket message for activity {} update",
                         activityId,
                         e);
              }
            }
          });
          relationshipManager.getAllWithListAccess(null);
        } finally {
          RequestLifeCycle.end();
          ExoContainerContext.setCurrentContainer(null);
        }
      });
    } else if (activity.getActivityStream().getType() == Type.SPACE) {
      Space space = spaceService.getSpaceByPrettyName(activity.getActivityStream().getPrettyId());
      if (space != null) {
        // Send to space connected members only
        Set<String> connectedUserIds = continuationBayeux.getConnectedUserIds();
        if (CollectionUtils.isNotEmpty(connectedUserIds)) {
          executorService.execute(() -> {
            RequestLifeCycle.begin(portalContainer);
            ExoContainerContext.setCurrentContainer(portalContainer);
            try {
              connectedUserIds.forEach(connectedUserId -> {
                if (spaceService.isMember(space, connectedUserId)) {
                  try {
                    continuationService.sendMessage(connectedUserId, COMETD_CHANNEL, message);
                  } catch (Exception e) {
                    LOG.warn("Error sending WebSocket message for activity {} update",
                             activityId,
                             e);
                  }
                }
              });
            } finally {
              RequestLifeCycle.end();
              ExoContainerContext.setCurrentContainer(null);
            }
          });
        }
      }
    }
  }

}
