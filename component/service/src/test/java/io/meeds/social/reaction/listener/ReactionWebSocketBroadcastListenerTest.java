/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.social.reaction.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.websocket.ActivityStreamWebSocketService;

import io.meeds.social.reaction.model.Reaction;
import io.meeds.social.reaction.service.ReactionService;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ReactionWebSocketBroadcastListenerTest {

  @Mock
  private ListenerService                listenerService;

  @Mock
  private ActivityManager                activityManager;

  @Mock
  private SpaceService                   spaceService;

  @Mock
  private ActivityStreamWebSocketService activityStreamWebSocketService;

  @InjectMocks
  private ReactionWebSocketBroadcastListener listener;

  @Test
  public void initListensToTheCreationAndTheUpdate() {
    listener.init();

    verify(listenerService).addListener(ReactionService.REACTION_CREATED_EVENT_NAME, listener);
    verify(listenerService).addListener(ReactionService.REACTION_UPDATED_EVENT_NAME, listener);
  }

  @Test
  public void aReactionOnAnActivityRefreshesItsStream() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setId("1");
    when(activityManager.getActivity("1")).thenReturn(activity);

    listener.onEvent(new Event<>(ReactionService.REACTION_CREATED_EVENT_NAME, new Reaction(7, "like", "activity", "1"), "john"));

    verify(activityStreamWebSocketService).sendMessage(any());
  }

  @Test
  public void aReactionOnAnotherObjectTypeIsNotReadAsAnActivityId() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setId("1");
    when(activityManager.getActivity("1")).thenReturn(activity);

    listener.onEvent(new Event<>(ReactionService.REACTION_CREATED_EVENT_NAME, new Reaction(7, "like", "taskComment", "1"), "john"));

    verify(activityManager, never()).getActivity("1");
    verify(activityStreamWebSocketService, never()).sendMessage(any());
  }

  @Test
  public void aMissingOrHiddenActivityIsIgnored() throws Exception {
    listener.onEvent(new Event<>(ReactionService.REACTION_UPDATED_EVENT_NAME, new Reaction(7, "like", "activity", "404"), "john"));
    ExoSocialActivity hidden = mock(ExoSocialActivity.class);
    when(hidden.isHidden()).thenReturn(true);
    when(activityManager.getActivity("2")).thenReturn(hidden);
    listener.onEvent(new Event<>(ReactionService.REACTION_UPDATED_EVENT_NAME, new Reaction(7, "like", "activity", "2"), "john"));

    verify(activityStreamWebSocketService, never()).sendMessage(any());
  }

}
