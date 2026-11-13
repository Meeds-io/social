/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.websocket.listener;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.notification.service.SpaceWebNotificationService;
import org.exoplatform.social.websocket.SpaceWebNotificationWebSocketService;

public class SpaceWebNotificationWebSocketServerListenerTest {

  private static final String                  READ_MESSAGE =
                                                            "{\"wsEventName\":\""
                                                                + SpaceWebNotificationService.NOTIFICATION_READ_EVENT_NAME
                                                                + "\",\"message\":{\"spaceWebNotificationItem\":\"{\\\"userId\\\":5,\\\"spaceId\\\":7}\"}}";

  private SpaceWebNotificationWebSocketService service;

  private SpaceWebNotificationWebSocketServerListener listener;

  private ServerChannel                        channel;

  private ServerMessage.Mutable                message;

  @Before
  public void setUp() {
    service = mock(SpaceWebNotificationWebSocketService.class);
    listener = new SpaceWebNotificationWebSocketServerListener(mock(PortalContainer.class),
                                                               service,
                                                               SpaceWebNotificationWebSocketService.COMETD_CHANNEL);
    channel = mock(ServerChannel.class);
    when(channel.getId()).thenReturn(SpaceWebNotificationWebSocketService.COMETD_CHANNEL);
    message = mock(ServerMessage.Mutable.class);
    when(message.getData()).thenReturn(READ_MESSAGE);
  }

  @Test
  public void testAClientsMessageIsHandled() throws Exception {
    ServerSession client = mock(ServerSession.class);
    when(client.getId()).thenReturn("client");

    assertTrue(listener.onMessage(client, channel, message));

    verify(service).receiveMessage(eq("client"),
                                   eq(SpaceWebNotificationService.NOTIFICATION_READ_EVENT_NAME),
                                   any());
  }

  @Test
  public void testAMessageRelayedFromAnotherNodeIsHandledThereOnly() throws Exception {
    ServerSession oortSession = mock(ServerSession.class);
    when(service.isRelayed(oortSession)).thenReturn(true);

    assertFalse(listener.onMessage(oortSession, channel, message));

    verify(service, never()).receiveMessage(any(), any(), any());
  }
}
