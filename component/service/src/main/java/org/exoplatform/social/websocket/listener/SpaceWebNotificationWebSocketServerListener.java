/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
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
package org.exoplatform.social.websocket.listener;

import java.io.ByteArrayInputStream;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage.Mutable;
import org.cometd.bayeux.server.ServerSession;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.notification.model.SpaceWebNotificationItemUpdate;
import org.exoplatform.social.websocket.SpaceWebNotificationWebSocketService;
import org.exoplatform.social.websocket.entity.WebSocketMessage;
import org.exoplatform.ws.frameworks.json.impl.JsonDefaultHandler;
import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonParserImpl;
import org.exoplatform.ws.frameworks.json.impl.ObjectBuilder;

public class SpaceWebNotificationWebSocketServerListener implements ServerChannel.MessageListener {
  private static final String                  SPACE_WEB_NOTIFICATION_ITEM_MAP_KEY = "spaceWebNotificationItem";

  private static final Log                     LOG                                 =
                                                   ExoLogger.getLogger(SpaceWebNotificationWebSocketServerListener.class);

  private PortalContainer                      container;

  private SpaceWebNotificationWebSocketService spaceWebNotificationWebSocketService;

  private String                               cometdChannel;

  public SpaceWebNotificationWebSocketServerListener(PortalContainer container,
                                                     SpaceWebNotificationWebSocketService spaceWebNotificationWebSocketService,
                                                     String cometdChannel) {
    this.container = container;
    this.spaceWebNotificationWebSocketService = spaceWebNotificationWebSocketService;
    this.cometdChannel = cometdChannel;
  }

  @Override
  public boolean onMessage(ServerSession from, ServerChannel channel, Mutable message) {
    if (message == null || message.getData() == null) {
      LOG.warn("Empty analytics WebSocket message is received");
      return false;
    }
    if (from == null) {
      LOG.warn("Empty analytics WebSocket session is received");
      return false;
    }
    if (channel == null) {
      LOG.warn("Empty WebSocket channel received");
      return false;
    }
    if (!StringUtils.equals(channel.getId(), cometdChannel)) {
      LOG.debug("Not adequate channel name {}", channel.getId());
      return false;
    }

    Object data = message.getData();
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      WebSocketMessage webSocketMessage = fromJsonString(data.toString());
      SpaceWebNotificationItemUpdate spaceWebNotificationItem = (SpaceWebNotificationItemUpdate) webSocketMessage.getMessage()
                                                                                                                 .get(SPACE_WEB_NOTIFICATION_ITEM_MAP_KEY);
      spaceWebNotificationWebSocketService.receiveMessage(from.getId(),
                                                          webSocketMessage.getWsEventName(),
                                                          spaceWebNotificationItem);
      return true;
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.warn("Error marking as read", e);
      } else {
        LOG.warn("Error when parsing Web Socket message: {}", e.getMessage());
      }
      return false;
    } finally {
      RequestLifeCycle.end();
      ExoContainerContext.setCurrentContainer(null);
    }
  }

  private WebSocketMessage fromJsonString(String data) {
    try {
      JsonDefaultHandler wsMessageHandler = new JsonDefaultHandler();
      new JsonParserImpl().parse(new ByteArrayInputStream(data.getBytes()), wsMessageHandler);
      String wsEventName = wsMessageHandler.getJsonObject().getElement("wsEventName").getStringValue();
      String notificationItemString = wsMessageHandler.getJsonObject()
                                                      .getElement("message")
                                                      .getElement(SPACE_WEB_NOTIFICATION_ITEM_MAP_KEY)
                                                      .getStringValue();

      JsonDefaultHandler notificationItemHandler = new JsonDefaultHandler();
      new JsonParserImpl().parse(new ByteArrayInputStream(notificationItemString.getBytes()), notificationItemHandler);
      SpaceWebNotificationItemUpdate spaceWebNotificationItem = ObjectBuilder.createObject(SpaceWebNotificationItemUpdate.class,
                                                                                           notificationItemHandler.getJsonObject());
      return new WebSocketMessage(wsEventName,
                                  Collections.singletonMap(SPACE_WEB_NOTIFICATION_ITEM_MAP_KEY, spaceWebNotificationItem));
    } catch (JsonException e) {
      throw new IllegalStateException("Error when parsing Web Socket message", e);
    }
  }

}
