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
package io.meeds.social.report.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.websocket.ActivityStreamWebSocketService;
import org.exoplatform.social.websocket.entity.ActivityStreamModification;

import io.meeds.social.report.model.ActivityReport;
import io.meeds.social.report.service.ActivityReportService;

@RunWith(MockitoJUnitRunner.class)
public class ReportWebSocketBroadcastListenerTest {

  @Mock
  private ListenerService                    listenerService;

  @Mock
  private ActivityManager                    activityManager;

  @Mock
  private SpaceService                       spaceService;

  @Mock
  private ActivityStreamWebSocketService     activityStreamWebSocketService;

  @InjectMocks
  private ReportWebSocketBroadcastListener   listener;

  @Test
  public void testRegistersOnReportedEvent() {
    listener.init();
    verify(listenerService).addListener(ActivityReportService.EVENT_ACTIVITY_REPORTED, listener);
  }

  @Test
  public void testBroadcastsActivityUpdateForReportedPost() throws Exception {
    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    when(activity.getId()).thenReturn("55");
    when(activity.isComment()).thenReturn(false);
    when(activityManager.getActivity("55")).thenReturn(activity);

    listener.onEvent(new Event<>(ActivityReportService.EVENT_ACTIVITY_REPORTED, report("55", null), 999L));

    ArgumentCaptor<ActivityStreamModification> captor = ArgumentCaptor.forClass(ActivityStreamModification.class);
    verify(activityStreamWebSocketService).sendMessage(captor.capture());
    assertEquals("updateActivity", captor.getValue().getEventName());
    assertEquals("55", captor.getValue().getActivityId());
    assertNull(captor.getValue().getCommentId());
  }

  @Test
  public void testBroadcastsCommentUpdateForReportedComment() throws Exception {
    ExoSocialActivity comment = mock(ExoSocialActivity.class);
    when(comment.getId()).thenReturn("comment6");
    when(comment.isComment()).thenReturn(true);
    when(comment.getParentId()).thenReturn("55");
    when(activityManager.getActivity("comment6")).thenReturn(comment);
    lenient().when(activityManager.getActivity("55")).thenReturn(mock(ExoSocialActivity.class));

    listener.onEvent(new Event<>(ActivityReportService.EVENT_ACTIVITY_REPORTED, report("comment6", "55"), 999L));

    ArgumentCaptor<ActivityStreamModification> captor = ArgumentCaptor.forClass(ActivityStreamModification.class);
    verify(activityStreamWebSocketService).sendMessage(captor.capture());
    assertEquals("updateComment", captor.getValue().getEventName());
    assertEquals("55", captor.getValue().getActivityId());
    assertEquals("comment6", captor.getValue().getCommentId());
  }

  @Test
  public void testNoBroadcastForMissingOrHiddenActivity() throws Exception {
    when(activityManager.getActivity("55")).thenReturn(null);
    listener.onEvent(new Event<>(ActivityReportService.EVENT_ACTIVITY_REPORTED, report("55", null), 999L));

    ExoSocialActivity hidden = mock(ExoSocialActivity.class);
    when(hidden.isHidden()).thenReturn(true);
    when(activityManager.getActivity("55")).thenReturn(hidden);
    listener.onEvent(new Event<>(ActivityReportService.EVENT_ACTIVITY_REPORTED, report("55", null), 999L));

    verifyNoInteractions(activityStreamWebSocketService);
  }

  private ActivityReport report(String activityId, String parentId) {
    return new ActivityReport(activityId, "activity", activityId, parentId, 999L, "spam", 5L);
  }

}
