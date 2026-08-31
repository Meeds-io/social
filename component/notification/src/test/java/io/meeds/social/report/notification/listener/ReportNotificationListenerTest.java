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
package io.meeds.social.report.notification.listener;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.services.listener.Event;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.report.model.ActivityReport;

@RunWith(MockitoJUnitRunner.class)
public class ReportNotificationListenerTest {

  @Mock
  private ActivityManager activityManager;

  @Mock
  private IdentityManager identityManager;

  @Mock
  private SpaceService    spaceService;

  @Test
  public void testNoNotificationWhenActivityIsMissing() throws Exception {
    ReportNotificationListener listener = new ReportNotificationListener(activityManager, identityManager, spaceService);
    when(activityManager.getActivity("55")).thenReturn(null);

    listener.onEvent(new Event<>("social.activity.reported", report(), 999L));

    verifyNoInteractions(spaceService);
  }

  @Test
  public void testNoNotificationWhenSpaceIsMissing() throws Exception {
    ReportNotificationListener listener = new ReportNotificationListener(activityManager, identityManager, spaceService);
    when(activityManager.getActivity("55")).thenReturn(mock(ExoSocialActivity.class));
    Identity identity = mock(Identity.class);
    when(identity.getRemoteId()).thenReturn("testspace");
    when(identityManager.getIdentity(anyString())).thenReturn(identity);
    when(spaceService.getSpaceByPrettyName("testspace")).thenReturn(null);

    listener.onEvent(new Event<>("social.activity.reported", report(), 999L));

    verify(spaceService).getSpaceByPrettyName("testspace");
  }

  private ActivityReport report() {
    return new ActivityReport("55", "activity", "55", null, 999L, "spam", 5L);
  }

}
