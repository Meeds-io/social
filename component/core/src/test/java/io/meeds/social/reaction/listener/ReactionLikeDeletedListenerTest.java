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

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;

import io.meeds.social.reaction.service.ReactionService;
import io.meeds.social.reaction.service.ReactionServiceImpl;

@ExtendWith(MockitoExtension.class)
public class ReactionLikeDeletedListenerTest {

  @Mock
  private ActivityManager            activityManager;

  @Mock
  private ReactionService            reactionService;

  @Mock
  private ActivityLifeCycleEvent     event;

  @Mock
  private ExoSocialActivity          activity;

  @InjectMocks
  private ReactionLikeDeletedListener listener;

  @BeforeEach
  public void setUp() {
    lenient().when(event.getActivity()).thenReturn(activity);
    lenient().when(activity.getId()).thenReturn("55");
    lenient().when(event.getUserId()).thenReturn("123");
  }

  @Test
  public void testInitRegistersTheListener() {
    listener.init();
    verify(activityManager).addActivityEventListener(listener);
  }

  @Test
  public void testDeleteLikeActivityDelegatesToTheService() {
    listener.deleteLikeActivity(event);
    verify(reactionService).deleteReactionItem(ReactionServiceImpl.ACTIVITY_OBJECT_TYPE, "55", 123l);
  }

  @Test
  public void testDeleteLikeCommentDelegatesToTheService() {
    when(activity.getId()).thenReturn("comment66");
    listener.deleteLikeComment(event);
    verify(reactionService).deleteReactionItem(ReactionServiceImpl.ACTIVITY_OBJECT_TYPE, "comment66", 123l);
  }

}
