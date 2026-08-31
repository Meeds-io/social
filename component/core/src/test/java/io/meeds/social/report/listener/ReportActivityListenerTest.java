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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;

import io.meeds.social.report.service.ActivityReportService;

@RunWith(MockitoJUnitRunner.class)
public class ReportActivityListenerTest {

  @Mock
  private ActivityManager       activityManager;

  @Mock
  private ActivityReportService activityReportService;

  @InjectMocks
  private ReportActivityListener listener;

  @Test
  public void testRegistersItselfOnActivityLifecycle() {
    listener.init();
    verify(activityManager).addActivityEventListener(listener);
  }

  @Test
  public void testDelegatesActivityAndCommentContentEditsToTheService() {
    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    ActivityLifeCycleEvent event = mock(ActivityLifeCycleEvent.class);
    when(event.getActivity()).thenReturn(activity);
    when(event.isContentChanged()).thenReturn(true);

    listener.updateActivity(event);
    listener.updateComment(event);

    verify(activityReportService, org.mockito.Mockito.times(2)).markReportsStale(activity);
  }

  @Test
  public void testContentNeutralUpdatesNeverFlipReportsStale() {
    // category link/unlink, unhide and other content-neutral paths broadcast
    // the update lifecycle with contentChanged = false: the Reported state
    // must hold (US03: it resets only when the author edits the content)
    ActivityLifeCycleEvent event = mock(ActivityLifeCycleEvent.class);
    when(event.isContentChanged()).thenReturn(false);

    listener.updateActivity(event);
    listener.updateComment(event);

    verify(activityReportService, org.mockito.Mockito.never()).markReportsStale(org.mockito.ArgumentMatchers.any());
  }

}
