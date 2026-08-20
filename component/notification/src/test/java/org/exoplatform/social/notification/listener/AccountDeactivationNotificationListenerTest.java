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
package org.exoplatform.social.notification.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.command.NotificationCommand;
import org.exoplatform.commons.api.notification.command.NotificationExecutor;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.listener.Event;
import org.exoplatform.social.notification.plugin.AccountDeactivationRequestPlugin;

@RunWith(MockitoJUnitRunner.class)
public class AccountDeactivationNotificationListenerTest {

  private static final String                     REQUESTER_ID = "jdoe";

  private AccountDeactivationNotificationListener listener     = new AccountDeactivationNotificationListener();

  @Test
  public void testOnEventExecutesNotificationCommandWithRequester() throws Exception {
    NotificationContext ctx = mock(NotificationContext.class);
    NotificationExecutor executor = mock(NotificationExecutor.class);
    NotificationCommand command = mock(NotificationCommand.class);

    when(ctx.append(AccountDeactivationRequestPlugin.REQUESTER, REQUESTER_ID)).thenReturn(ctx);
    when(ctx.getNotificationExecutor()).thenReturn(executor);
    when(ctx.makeCommand(PluginKey.key(AccountDeactivationRequestPlugin.ID))).thenReturn(command);
    when(executor.with(command)).thenReturn(executor);

    try (MockedStatic<NotificationContextImpl> mockedStatic = mockStatic(NotificationContextImpl.class)) {
      mockedStatic.when(NotificationContextImpl::cloneInstance).thenReturn(ctx);

      listener.onEvent(new Event<>("social.account.deactivation.requested", REQUESTER_ID, "5"));

      verify(ctx).append(AccountDeactivationRequestPlugin.REQUESTER, REQUESTER_ID);
      verify(executor).with(command);
      verify(executor).execute(ctx);
    }
  }
}
