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
package org.exoplatform.social.websocket.listenr;

import org.exoplatform.social.websocket.SpaceWebNotificationWebSocketService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;

/**
 * A listener that will be triggered synchronously after marking an item as
 * unread to transmit modifications to end users via WebSocket
 */
@Asynchronous
public class SpaceWebNotificationWebSocketEventListener extends Listener<SpaceWebNotificationItem, Long> {

  private PortalContainer                      container;

  private SpaceWebNotificationWebSocketService spaceWebNotificationWebSocketService;

  public SpaceWebNotificationWebSocketEventListener(PortalContainer container,
                                                    SpaceWebNotificationWebSocketService spaceWebNotificationWebSocketService) {
    this.container = container;
    this.spaceWebNotificationWebSocketService = spaceWebNotificationWebSocketService;
  }

  @Override
  public void onEvent(Event<SpaceWebNotificationItem, Long> event) throws Exception {
    RequestLifeCycle.begin(container);
    try {
      SpaceWebNotificationItem spaceWebNotificationItem = event.getSource();
      spaceWebNotificationWebSocketService.sendMessage(event.getEventName(), spaceWebNotificationItem);
    } finally {
      RequestLifeCycle.end();
    }
  }

}
