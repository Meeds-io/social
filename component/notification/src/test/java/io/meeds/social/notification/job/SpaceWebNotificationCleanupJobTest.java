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
package io.meeds.social.notification.job;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.social.notification.service.SpaceWebNotificationService;

/**
 * The cleanup job is glue: it asks the service once, with one limit date, to
 * mark the old unread items as read, and nothing else.
 */
@RunWith(MockitoJUnitRunner.class)
public class SpaceWebNotificationCleanupJobTest {

  private static final int               KEEP_ALIVE_DAYS = 30;

  @Mock
  private SpaceWebNotificationService    spaceWebNotificationService;

  @InjectMocks
  private SpaceWebNotificationCleanupJob job;

  @Before
  public void setUp() throws Exception {
    // The @Value property, without a Spring context
    Field keepAliveDays = SpaceWebNotificationCleanupJob.class.getDeclaredField("keepAliveDays");
    keepAliveDays.setAccessible(true);
    keepAliveDays.setInt(job, KEEP_ALIVE_DAYS);
  }

  @Test
  public void testRunAsksTheServiceOnceWithOneLimitDate() {
    when(spaceWebNotificationService.markAllAsReadUntil(anyLong())).thenReturn(3);

    job.run();

    ArgumentCaptor<Long> untilDate = ArgumentCaptor.forClass(Long.class);
    verify(spaceWebNotificationService, times(1)).markAllAsReadUntil(untilDate.capture());
    assertTrue(untilDate.getValue() > 0);
  }

  @Test
  public void testRunWithNothingToMarkStillAsksOnce() {
    when(spaceWebNotificationService.markAllAsReadUntil(anyLong())).thenReturn(0);

    job.run();

    verify(spaceWebNotificationService, times(1)).markAllAsReadUntil(anyLong());
  }

}
