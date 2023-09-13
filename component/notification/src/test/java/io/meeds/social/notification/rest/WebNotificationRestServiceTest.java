/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
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
package io.meeds.social.notification.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.mockito.Mock;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.service.rest.BaseRestServicesTestCase;

public class WebNotificationRestServiceTest extends BaseRestServicesTestCase { // NOSONAR

  @Mock
  WebNotificationService webNotificationService;

  @Mock
  IdentityManager        identityManager;

  @Mock
  SpaceService           spaceService;

  @Override
  protected Class<?> getComponentClass() {
    return WebNotificationRestService.class;
  }

  public void testUnauthorizedMarkAsRead() {
    startSessionAs("john");

    webNotificationService = mock(WebNotificationService.class);
    NotificationInfo notificationInfo = new NotificationInfo();
    notificationInfo.setTo("mary");
    when(webNotificationService.getNotificationInfo(anyString())).thenReturn(notificationInfo);

    WebNotificationRestService webNotificationRestService = newWebNotificationRestService();
    Response response = webNotificationRestService.updateNotification("markAsRead", "1");

    assertEquals(401, response.getStatus()); // NOSONAR
  }

  public void testAuthorizedMarkAsRead() {
    startSessionAs("john");

    webNotificationService = mock(WebNotificationService.class);

    NotificationInfo notificationInfo = new NotificationInfo();
    notificationInfo.setTo("john");

    when(webNotificationService.getNotificationInfo(anyString())).thenReturn(notificationInfo);

    WebNotificationRestService webNotificationRestService = newWebNotificationRestService();
    Response response = webNotificationRestService.updateNotification("markAsRead", "1");

    assertEquals(204, response.getStatus());

  }

  public void testUnauthorizedHide() {
    startSessionAs("john");

    webNotificationService = mock(WebNotificationService.class);

    NotificationInfo notificationInfo = new NotificationInfo();
    notificationInfo.setTo("mary");
    when(webNotificationService.getNotificationInfo(anyString())).thenReturn(notificationInfo);

    WebNotificationRestService webNotificationRestService = newWebNotificationRestService();
    Response response = webNotificationRestService.hideNotifications("1");

    assertEquals(response.getStatus(), 401);

  }

  public void testAuthorizedHide() {
    startSessionAs("john");

    webNotificationService = mock(WebNotificationService.class);

    NotificationInfo notificationInfo = new NotificationInfo();
    notificationInfo.setTo("john");
    when(webNotificationService.getNotificationInfo(anyString())).thenReturn(notificationInfo);

    WebNotificationRestService webNotificationRestService = newWebNotificationRestService();
    Response response = webNotificationRestService.hideNotifications("1");

    assertEquals(204, response.getStatus());

  }

  private void startSessionAs(String username) {
    Identity identity = new Identity(username);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  private WebNotificationRestService newWebNotificationRestService() {
    return new WebNotificationRestService(webNotificationService, identityManager, spaceService);
  }

}
