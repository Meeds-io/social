/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.exoplatform.social.notification.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;

@RunWith(MockitoJUnitRunner.class)
public class AccountDeactivationRequestPluginTest {

  private static final String              REQUESTER_ID = "jdoe";

  @Mock
  private InitParams                       initParams;

  @Mock
  private OrganizationService              organizationService;

  @Mock
  private UserHandler                      userHandler;

  @Mock
  private ListAccess<User>                 administrators;

  private AccountDeactivationRequestPlugin plugin;

  @Before
  public void setUp() {
    plugin = new AccountDeactivationRequestPlugin(initParams);
  }

  @Test
  public void testGetId() {
    assertEquals(AccountDeactivationRequestPlugin.ID, plugin.getId());
  }

  @Test
  public void testIsValidOnlyWithRequester() {
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    assertFalse(plugin.isValid(ctx));

    ctx.append(AccountDeactivationRequestPlugin.REQUESTER, REQUESTER_ID);
    assertTrue(plugin.isValid(ctx));
  }

  @Test
  public void testMakeNotificationTargetsAdministratorsWithoutRequester() throws Exception {
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(AccountDeactivationRequestPlugin.REQUESTER, REQUESTER_ID);
    // the requester is still member of the administrators group: they must not
    // be notified of their own deactivation request
    User[] users = { mockUser("admin1"), mockUser(REQUESTER_ID), mockUser("admin2") };
    when(administrators.getSize()).thenReturn(3);
    when(administrators.load(0, 3)).thenReturn(users);

    try (MockedStatic<CommonsUtils> commonsUtils = mockStatic(CommonsUtils.class)) {
      commonsUtils.when(() -> CommonsUtils.getService(OrganizationService.class)).thenReturn(organizationService);
      when(organizationService.getUserHandler()).thenReturn(userHandler);
      when(userHandler.findUsersByGroupId(AccountDeactivationRequestPlugin.ADMINISTRATORS_GROUP)).thenReturn(administrators);

      NotificationInfo notification = plugin.makeNotification(ctx);

      assertNotNull(notification);
      assertEquals(plugin.getKey(), notification.getKey());
      assertEquals(Arrays.asList("admin1", "admin2"), notification.getSendToUserIds());
      assertEquals(REQUESTER_ID, notification.getFrom());
      assertEquals(REQUESTER_ID, notification.getValueOwnerParameter(SocialNotificationUtils.REMOTE_ID.getKey()));
    }
  }

  @Test
  public void testMakeNotificationWhenRequesterIsTheOnlyAdministrator() throws Exception {
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(AccountDeactivationRequestPlugin.REQUESTER, REQUESTER_ID);
    User[] users = { mockUser(REQUESTER_ID) };
    when(administrators.getSize()).thenReturn(1);
    when(administrators.load(0, 1)).thenReturn(users);

    try (MockedStatic<CommonsUtils> commonsUtils = mockStatic(CommonsUtils.class)) {
      commonsUtils.when(() -> CommonsUtils.getService(OrganizationService.class)).thenReturn(organizationService);
      when(organizationService.getUserHandler()).thenReturn(userHandler);
      when(userHandler.findUsersByGroupId(AccountDeactivationRequestPlugin.ADMINISTRATORS_GROUP)).thenReturn(administrators);

      assertNull(plugin.makeNotification(ctx));
    }
  }

  @Test
  public void testMakeNotificationWhenAdministratorsResolutionFails() throws Exception {
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(AccountDeactivationRequestPlugin.REQUESTER, REQUESTER_ID);

    try (MockedStatic<CommonsUtils> commonsUtils = mockStatic(CommonsUtils.class)) {
      commonsUtils.when(() -> CommonsUtils.getService(OrganizationService.class)).thenReturn(organizationService);
      when(organizationService.getUserHandler()).thenReturn(userHandler);
      when(userHandler.findUsersByGroupId(AccountDeactivationRequestPlugin.ADMINISTRATORS_GROUP))
                                                                                                 .thenThrow(new IllegalStateException("fake failure"));

      // the failure must be swallowed into the context, not propagated to the
      // deactivation flow
      assertNull(plugin.makeNotification(ctx));
    }
  }

  private User mockUser(String username) {
    User user = mock(User.class);
    when(user.getUserName()).thenReturn(username);
    return user;
  }
}
