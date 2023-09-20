/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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

package io.meeds.social.observer.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.social.observe.plugin.ActivityOberverPlugin;

@RunWith(MockitoJUnitRunner.class)
public class ActivityOberverPluginTest {

  private static final String                        AUDIENCE_ID = "5543";

  private static final String                        SPACE_ID    = "5743";

  private static final String                        USER_NAME   = "testUser";

  private static final int                           IDENTITY_ID = 2;

  private static final String                        OBJECT_ID   = "objectId";

  @Mock
  private IdentityRegistry                           identityRegistry;

  @Mock
  private ActivityManager                            activityManager;

  @Mock
  private IdentityManager                            identityManager;

  @Mock
  private OrganizationService                        organizationService;

  @Mock
  private ExoSocialActivity                          activity;

  @Mock
  private Identity                                   identity;

  @Mock
  private ActivityStream                             activityStream;

  @Mock
  private org.exoplatform.services.security.Identity aclIdentity;

  ActivityOberverPlugin                              activityOberverPlugin;

  @Before
  public void setUp() {
    activityOberverPlugin = new ActivityOberverPlugin(activityManager, identityManager, organizationService, identityRegistry);
    when(identity.getRemoteId()).thenReturn(USER_NAME);
    when(activity.getSpaceId()).thenReturn(SPACE_ID);
    when(activity.getActivityStream()).thenReturn(activityStream);
    when(activityStream.getId()).thenReturn(AUDIENCE_ID);
  }

  @Test
  public void testGetObjectType() {
    assertEquals(ActivityOberverPlugin.OBJECT_TYPE, activityOberverPlugin.getObjectType());
  }

  @Test
  public void testCanObserve() throws ObjectNotFoundException {
    assertThrows(ObjectNotFoundException.class, () -> activityOberverPlugin.canObserve(IDENTITY_ID, OBJECT_ID));
    when(activityManager.getActivity(OBJECT_ID)).thenReturn(activity);
    assertThrows(ObjectNotFoundException.class, () -> activityOberverPlugin.canObserve(IDENTITY_ID, OBJECT_ID));
    when(identityManager.getIdentity(String.valueOf(IDENTITY_ID))).thenReturn(identity);
    assertThrows(IllegalStateException.class, () -> activityOberverPlugin.canObserve(IDENTITY_ID, OBJECT_ID));
    when(identityRegistry.getIdentity(USER_NAME)).thenReturn(aclIdentity);
    assertFalse(activityOberverPlugin.canObserve(IDENTITY_ID, OBJECT_ID));
    when(activityManager.isActivityViewable(activity, aclIdentity)).thenReturn(true);
    assertTrue(activityOberverPlugin.canObserve(IDENTITY_ID, OBJECT_ID));
  }

  @Test
  public void testGetAudienceId() throws ObjectNotFoundException {
    assertThrows(ObjectNotFoundException.class, () -> activityOberverPlugin.getAudienceId(OBJECT_ID));
    when(activityManager.getActivity(OBJECT_ID)).thenReturn(activity);
    assertEquals(Long.parseLong(AUDIENCE_ID), activityOberverPlugin.getAudienceId(OBJECT_ID));
  }

  @Test
  public void testGetSpaceId() throws ObjectNotFoundException {
    assertThrows(ObjectNotFoundException.class, () -> activityOberverPlugin.getSpaceId(OBJECT_ID));
    when(activityManager.getActivity(OBJECT_ID)).thenReturn(activity);
    assertEquals(0l, activityOberverPlugin.getSpaceId(OBJECT_ID));
    when(activityStream.isSpace()).thenReturn(true);
    assertEquals(Long.parseLong(SPACE_ID), activityOberverPlugin.getSpaceId(OBJECT_ID));
  }

}
