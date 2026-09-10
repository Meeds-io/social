/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package org.exoplatform.social.websocket;

import static org.exoplatform.social.notification.service.SpaceWebNotificationService.NOTIFICATION_READ_EVENT_NAME;
import static org.exoplatform.social.notification.service.SpaceWebNotificationService.NOTIFICATION_UNREAD_EVENT_NAME;
import static org.exoplatform.social.websocket.SpaceWebNotificationWebSocketService.COMETD_CHANNEL;
import static org.exoplatform.social.websocket.SpaceWebNotificationWebSocketService.OUTBOUND_ITEM_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;
import org.exoplatform.social.notification.model.SpaceWebNotificationItemUpdate;
import org.exoplatform.social.notification.service.SpaceWebNotificationService;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.exoplatform.ws.frameworks.json.impl.JsonDefaultHandler;
import org.exoplatform.ws.frameworks.json.impl.JsonParserImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;

@RunWith(MockitoJUnitRunner.class)
public class SpaceWebNotificationWebSocketServiceTest {

  private static final long                    USER_IDENTITY_ID = 5l;

  private static final String                  USERNAME         = "john";

  private static final long                    SPACE_ID         = 7l;

  @Mock
  private SpaceWebNotificationService          spaceWebNotificationService;

  @Mock
  private ContinuationService                  continuationService;

  @Mock
  private EXoContinuationBayeux                continuationBayeux;

  @Mock
  private SpaceService                         spaceService;

  @Mock
  private IdentityManager                      identityManager;

  private SpaceWebNotificationWebSocketService webSocketService;

  @Before
  public void setUp() {
    webSocketService = new SpaceWebNotificationWebSocketService(null,
                                                                spaceWebNotificationService,
                                                                continuationService,
                                                                continuationBayeux,
                                                                spaceService,
                                                                identityManager);
    Identity identity = new Identity(String.valueOf(USER_IDENTITY_ID));
    identity.setRemoteId(USERNAME);
    identity.setEnable(true);
    identity.setDeleted(false);
    when(identityManager.getIdentity(String.valueOf(USER_IDENTITY_ID))).thenReturn(identity);
    when(continuationService.isPresent(USERNAME)).thenReturn(true);
  }

  /**
   * Regression pin (EXO-89999): the item marked as read from the browser
   * reaches the server as a {@link SpaceWebNotificationItemUpdate}. The
   * message echoed back to the browser must still carry the item under the
   * key every frontend component reads, else the unread badge never
   * disappears.
   */
  @Test
  public void testSendMessageUsesStableKeyForItemUpdate() throws Exception {
    SpaceWebNotificationItemUpdate item = new SpaceWebNotificationItemUpdate();
    item.setApplicationName("activity");
    item.setApplicationItemId("100");
    item.setUserId(USER_IDENTITY_ID);
    item.setSpaceId(SPACE_ID);
    item.setUserEvent("click");

    webSocketService.sendMessage(NOTIFICATION_READ_EVENT_NAME, item);

    JsonValue message = captureSentMessage(NOTIFICATION_READ_EVENT_NAME);
    JsonValue sentItem = message.getElement(OUTBOUND_ITEM_KEY);
    assertNotNull("Item must be published under the key read by the frontend", sentItem);
    assertEquals("activity", sentItem.getElement("applicationName").getStringValue());
    assertEquals("100", sentItem.getElement("applicationItemId").getStringValue());
    assertEquals(SPACE_ID, sentItem.getElement("spaceId").getLongValue());
    assertEquals(USER_IDENTITY_ID, sentItem.getElement("userId").getLongValue());
  }

  @Test
  public void testSendMessageUsesStableKeyForServerBuiltItem() throws Exception {
    SpaceWebNotificationItem item = new SpaceWebNotificationItem("news", "42", USER_IDENTITY_ID, SPACE_ID);

    webSocketService.sendMessage(NOTIFICATION_UNREAD_EVENT_NAME, item);

    JsonValue message = captureSentMessage(NOTIFICATION_UNREAD_EVENT_NAME);
    JsonValue sentItem = message.getElement(OUTBOUND_ITEM_KEY);
    assertNotNull(sentItem);
    assertEquals("news", sentItem.getElement("applicationName").getStringValue());
    assertEquals("42", sentItem.getElement("applicationItemId").getStringValue());
  }

  @Test
  public void testSendMessageIgnoresOfflineUser() {
    when(continuationService.isPresent(USERNAME)).thenReturn(false);
    SpaceWebNotificationItem item = new SpaceWebNotificationItem("activity", "100", USER_IDENTITY_ID, SPACE_ID);

    webSocketService.sendMessage(NOTIFICATION_READ_EVENT_NAME, item);

    verify(continuationService, never()).sendMessage(anyString(), anyString(), any());
  }

  private JsonValue captureSentMessage(String expectedEventName) throws Exception {
    ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
    verify(continuationService).sendMessage(eq(USERNAME), eq(COMETD_CHANNEL), messageCaptor.capture());

    JsonDefaultHandler handler = new JsonDefaultHandler();
    new JsonParserImpl().parse(new ByteArrayInputStream(messageCaptor.getValue().getBytes(StandardCharsets.UTF_8)), handler);
    JsonValue wsMessage = handler.getJsonObject();
    assertEquals(expectedEventName, wsMessage.getElement("wsEventName").getStringValue());
    JsonValue message = wsMessage.getElement("message");
    assertNotNull(message);
    return message;
  }

}
