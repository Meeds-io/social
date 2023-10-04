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

import static org.exoplatform.social.notification.service.SpaceWebNotificationService.NOTIFICATION_ALL_READ_EVENT_NAME;
import static org.exoplatform.social.notification.service.SpaceWebNotificationService.NOTIFICATION_READ_EVENT_NAME;
import static org.exoplatform.social.notification.service.SpaceWebNotificationService.NOTIFICATION_UNREAD_EVENT_NAME;

import org.apache.commons.lang3.StringUtils;
import org.cometd.bayeux.server.ServerChannel;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;
import org.picocontainer.Startable;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;
import org.exoplatform.social.notification.service.SpaceWebNotificationService;
import org.exoplatform.social.websocket.entity.WebSocketMessage;
import org.exoplatform.social.websocket.listener.SpaceWebNotificationWebSocketServerListener;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;

public class SpaceWebNotificationWebSocketService implements Startable {

  public static final String          COMETD_CHANNEL = "/SpaceWebNotification";

  private PortalContainer             container;

  private SpaceWebNotificationService spaceWebNotificationService;

  private ContinuationService         continuationService;

  private EXoContinuationBayeux       continuationBayeux;

  private SpaceService                spaceService;

  private IdentityManager             identityManager;

  public SpaceWebNotificationWebSocketService(PortalContainer container,
                                              SpaceWebNotificationService spaceWebNotificationService,
                                              ContinuationService continuationService,
                                              EXoContinuationBayeux continuationBayeux,
                                              SpaceService spaceService,
                                              IdentityManager identityManager) {
    this.container = container;
    this.spaceWebNotificationService = spaceWebNotificationService;
    this.continuationService = continuationService;
    this.continuationBayeux = continuationBayeux;
    this.spaceService = spaceService;
    this.identityManager = identityManager;
  }

  @Override
  public void start() {
    ServerChannel serverChannel = continuationBayeux.createChannelIfAbsent(COMETD_CHANNEL).getReference();
    serverChannel.addListener(new SpaceWebNotificationWebSocketServerListener(container, this, COMETD_CHANNEL));
  }

  @Override
  public void stop() {
    // Nothing To stop
  }

  /**
   * Propagate an event from Backend to frontend through WebSocket Message when
   * {@link SpaceWebNotificationItem} is altered
   * 
   * @param eventName                event name that will allow Browser to
   *                                   distinguish which behavior to adopt in
   *                                   order to update UI
   * @param spaceWebNotificationItem The unread item object
   */
  public void sendMessage(String eventName, SpaceWebNotificationItem spaceWebNotificationItem) {
    long userId = spaceWebNotificationItem.getUserId();
    Identity identity = identityManager.getIdentity(String.valueOf(userId));
    if (identity != null && !identity.isDeleted() && identity.isEnable()
        && continuationService.isPresent(identity.getRemoteId())) {
      String wsMessage = new WebSocketMessage(eventName, spaceWebNotificationItem).toJsonString();
      continuationService.sendMessage(identity.getRemoteId(), COMETD_CHANNEL, wsMessage);
    }
  }

  /**
   * Called when a new message is received via WebSocket from frontend
   * 
   * @param  wsClientId               Websocket Message event name
   * @param  eventName                Event Name
   * @param  spaceWebNotificationItem {@link SpaceWebNotificationItem} sent via
   *                                    Websocket
   * @throws Exception                when an error occurred while changing
   *                                    notification state
   */
  public void receiveMessage(String wsClientId,
                             String eventName,
                             SpaceWebNotificationItem spaceWebNotificationItem) throws Exception {
    if (spaceWebNotificationItem == null) {
      throw new IllegalArgumentException("Message is empty");
    }
    long userId = spaceWebNotificationItem.getUserId();
    if (userId == 0) {
      throw new IllegalArgumentException("Empty userId");
    }
    Identity identity = identityManager.getIdentity(String.valueOf(userId));
    if (identity == null || identity.isDeleted() || !identity.isEnable()) {
      throw new IllegalAccessException("User Identity with id " + userId + " isn't valid");
    }
    String username = identity.getRemoteId();
    if (!continuationBayeux.isSubscribed(username, wsClientId)) {
      throw new IllegalAccessException("User " + username + " isn't subscribed with a valid WebSocket token.");
    }
    if (StringUtils.equals(eventName, NOTIFICATION_UNREAD_EVENT_NAME)) {
      spaceWebNotificationService.markAsUnread(spaceWebNotificationItem);
    } else if (StringUtils.equals(eventName, NOTIFICATION_READ_EVENT_NAME)) {
      spaceWebNotificationService.markAsRead(spaceWebNotificationItem);
    } else if (StringUtils.equals(eventName, NOTIFICATION_ALL_READ_EVENT_NAME)) {
      long spaceId = spaceWebNotificationItem.getSpaceId();
      if (spaceId == 0) {
        spaceWebNotificationService.markAllAsRead(userId);
      } else {
        Space space = spaceService.getSpaceById(String.valueOf(spaceId));
        if (space == null) {
          throw new IllegalArgumentException("Space with id " + spaceId + " doesn't exists");
        }
        if (!spaceService.isMember(space, username)) {
          throw new IllegalAccessException("User " + username + " isn't member of space " + space.getDisplayName());
        }
        spaceWebNotificationService.markAllAsRead(userId, spaceId);
      }
    }
  }

}
