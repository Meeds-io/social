/*
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
package io.meeds.social.notification.listener;

import io.meeds.social.space.plugin.SpaceInvitationLifeCycleEvent;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.command.NotificationCommand;
import org.exoplatform.commons.api.notification.command.NotificationExecutor;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SpaceMembershipNotificationListenerTest {

  private static final String INVITER_ID = "inviter1";
  private static final String INVITED_USER_ID = "user1";
  private static final Long SPACE_ID = 10L;
  private static final String SPACE_NAME = "Test Space";
  private static final String SPACE_AVATAR = "/avatar.png";
  private static final String FULL_NAME = "John Doe";

  @Mock
  private IdentityManager identityManager;

  @Mock
  private SpaceService spaceService;

  @InjectMocks
  private SpaceMembershipNotificationListener listener;

  @Test
  public void initShouldRegisterListener() {
    listener.init();
    verify(spaceService).registerSpaceLifeCycleListener(listener);
  }

  @Test
  public void testUserJoinedByInvitationLink() {

    SpaceInvitationLifeCycleEvent event = mock(SpaceInvitationLifeCycleEvent.class);
    Space space = mock(Space.class);
    Identity identity = mock(Identity.class);
    Profile profile = mock(Profile.class);
    NotificationContext ctx = mock(NotificationContext.class);
    NotificationExecutor executor = mock(NotificationExecutor.class);

    when(event.getInviterId()).thenReturn(INVITER_ID);
    when(event.getTarget()).thenReturn(INVITED_USER_ID);
    when(event.getSpace()).thenReturn(space);

    when(space.getSpaceId()).thenReturn(SPACE_ID);
    when(space.getAvatarUrl()).thenReturn(SPACE_AVATAR);
    when(space.getDisplayName()).thenReturn(SPACE_NAME);

    when(identityManager.getOrCreateUserIdentity(INVITED_USER_ID)).thenReturn(identity);

    when(identity.getProfile()).thenReturn(profile);
    when(profile.getFullName()).thenReturn(FULL_NAME);

    when(ctx.getNotificationExecutor()).thenReturn(executor);
    when(executor.with(nullable(NotificationCommand.class))).thenReturn(executor);

    try (MockedStatic<NotificationContextImpl> mockedStatic = mockStatic(NotificationContextImpl.class)) {

      mockedStatic.when(NotificationContextImpl::cloneInstance).thenReturn(ctx);

      listener.userJoinedByInvitationLink(event);

      verify(identityManager).getOrCreateUserIdentity(INVITED_USER_ID);
      verify(ctx, atLeastOnce()).append(any(ArgumentLiteral.class), any());
      verify(executor).with(Mockito.<NotificationCommand>any());
      verify(executor).execute(ctx);
    }
  }

  @Test
  public void testUserJoinedByInvitationLinkWhenIdentityIsNull() {

    SpaceInvitationLifeCycleEvent event = mock(SpaceInvitationLifeCycleEvent.class);
    Space space = mock(Space.class);

    when(event.getTarget()).thenReturn(INVITED_USER_ID);
    when(event.getSpace()).thenReturn(space);

    when(identityManager.getOrCreateUserIdentity(INVITED_USER_ID)).thenReturn(null);

    listener.userJoinedByInvitationLink(event);

    verify(identityManager).getOrCreateUserIdentity(INVITED_USER_ID);
    verifyNoMoreInteractions(identityManager);
  }
}
