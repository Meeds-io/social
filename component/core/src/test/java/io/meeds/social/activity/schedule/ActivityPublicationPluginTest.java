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
package io.meeds.social.activity.schedule;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.manager.ActivityManager;

@RunWith(MockitoJUnitRunner.class)
public class ActivityPublicationPluginTest {

  @Mock
  private ActivityManager           activityManager;

  @InjectMocks
  private ActivityPublicationPlugin activityPublicationPlugin;

  @Test
  public void shouldDelegateOperationsToActivityManager() {
    assertEquals(ActivityPublicationPlugin.OBJECT_TYPE, activityPublicationPlugin.getObjectType());

    when(activityManager.getScheduledActivityIds(1200l, 0, 10)).thenReturn(List.of("15"));
    assertEquals(List.of("15"), activityPublicationPlugin.getDueObjectIds(1200l, 0, 10));

    ExoSocialActivity publishedActivity = new ExoSocialActivityImpl();
    when(activityManager.publishScheduledActivity("15")).thenReturn(publishedActivity);
    assertEquals(publishedActivity, activityPublicationPlugin.publish("15"));
  }

}
