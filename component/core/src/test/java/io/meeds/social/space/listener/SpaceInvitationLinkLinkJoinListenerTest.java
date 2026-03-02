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
package io.meeds.social.space.listener;

import io.meeds.social.space.invitation.model.SpaceInvitationLink;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SpaceInvitationLinkLinkJoinListenerTest {

  private static final long SPACE_ID = 100L;
  private static final String USER_ID = "user1";
  private static final String INVITER_ID = "inviter1";

  @Mock
  private SpaceService spaceService;

  @Mock
  private SpaceLifeCycleEvent event;

  @Mock
  private Space space;

  @Mock
  private SpaceInvitationLink spaceInvitationLink;

  @InjectMocks
  private SpaceInvitationLinkJoinListener listener;

  @Test
  public void testInitShouldRegisterListener() {
    listener.init();
    verify(spaceService).registerSpaceLifeCycleListener(listener);
  }

  @Test
  public void joinedShouldTriggerAndRemoveInvitationWhenInvitationExists() {
    when(event.getSpace()).thenReturn(space);
    when(space.getSpaceId()).thenReturn(SPACE_ID);
    when(event.getTarget()).thenReturn(USER_ID);

    when(spaceService.getSpaceInvitationLink(SPACE_ID, USER_ID))
        .thenReturn(spaceInvitationLink);
    when(spaceInvitationLink.getInviterId()).thenReturn(INVITER_ID);

    listener.joined(event);

    verify(spaceService).triggerUserJoinedByInvitationLink(space, USER_ID, INVITER_ID);
    verify(spaceService).removeSpaceInvitationLink(SPACE_ID, USER_ID);
  }

  @Test
  public void joinedShouldDoNothingWhenInvitationDoesNotExist() {
    when(event.getSpace()).thenReturn(space);
    when(space.getSpaceId()).thenReturn(SPACE_ID);
    when(event.getTarget()).thenReturn(USER_ID);

    when(spaceService.getSpaceInvitationLink(SPACE_ID, USER_ID))
        .thenReturn(null);

    listener.joined(event);

    verify(spaceService, never()).triggerUserJoinedByInvitationLink(any(), any(), any());
    verify(spaceService, never()).removeSpaceInvitationLink(anyLong(), anyString());
  }

  @Test
  public void removePendingUserShouldRemoveLinkInvitation() {
    when(event.getSpace()).thenReturn(space);
    when(space.getSpaceId()).thenReturn(SPACE_ID);
    when(event.getTarget()).thenReturn(USER_ID);

    listener.removePendingUser(event);

    verify(spaceService).removeSpaceInvitationLink(SPACE_ID, USER_ID);
  }
}
