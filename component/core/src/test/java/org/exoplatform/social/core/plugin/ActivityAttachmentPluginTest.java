/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package org.exoplatform.social.core.plugin;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;

@RunWith(MockitoJUnitRunner.class)
public class ActivityAttachmentPluginTest {

  private ActivityAttachmentPlugin activityAttachmentplugin;

  @Mock
  private ActivityManager          activityManager;

  @Before
  public void setUp() throws Exception {
    activityAttachmentplugin = new ActivityAttachmentPlugin(activityManager);
  }

  @Test
  public void testHasAccessPermission() throws ObjectNotFoundException {
    String activityId = "10";
    org.exoplatform.services.security.Identity userIdentity = Mockito.mock(org.exoplatform.services.security.Identity.class);
    ExoSocialActivity activity = Mockito.mock(ExoSocialActivity.class);
    when(activityManager.getActivity(activityId)).thenReturn(activity);
    when(activityManager.isActivityEditable(activity, userIdentity)).thenReturn(true);
    assertTrue(activityAttachmentplugin.hasEditPermission(userIdentity, activityId));
  }

  @Test
  public void testHasEditPermission() throws ObjectNotFoundException {
    String activityId = "10";
    org.exoplatform.services.security.Identity userIdentity = Mockito.mock(org.exoplatform.services.security.Identity.class);
    ExoSocialActivity activity = Mockito.mock(ExoSocialActivity.class);
    when(activityManager.getActivity(activityId)).thenReturn(activity);
    when(activityManager.isActivityEditable(activity, userIdentity)).thenReturn(true);
    assertTrue(activityAttachmentplugin.hasEditPermission(userIdentity, activityId));
  }

}
