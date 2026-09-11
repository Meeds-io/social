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
package io.meeds.social.timezone.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.social.core.identity.model.Profile;

@RunWith(MockitoJUnitRunner.class)
public class UserTimeZoneServiceTest {

  private static final String USERNAME  = "ayoub";

  private static final String TIME_ZONE = "Africa/Tunis";

  @Mock
  private OrganizationService organizationService;

  @Mock
  private ListenerService     listenerService;

  @Mock
  private UserProfileHandler  userProfileHandler;

  @Mock
  private UserProfile         userProfile;

  private UserTimeZoneService userTimeZoneService;

  @Before
  public void setUp() {
    userTimeZoneService = new UserTimeZoneService(organizationService, listenerService);
    lenient().when(organizationService.getUserProfileHandler()).thenReturn(userProfileHandler);
  }

  @Test
  public void testGetUserTimeZone() throws Exception {
    when(userProfileHandler.findUserProfileByName(USERNAME)).thenReturn(userProfile);
    when(userProfile.getAttribute(Profile.USER_TIME_ZONE)).thenReturn(TIME_ZONE);

    assertEquals(TIME_ZONE, userTimeZoneService.getUserTimeZone(USERNAME));
  }

  @Test
  public void testNoTimeZoneBeforeTheFirstPageLoad() throws Exception {
    when(userProfileHandler.findUserProfileByName(USERNAME)).thenReturn(null);
    assertNull(userTimeZoneService.getUserTimeZone(USERNAME));
    assertNull(userTimeZoneService.getUserTimeZone(null));
  }

  @Test
  public void testAProfileReadFailureFallsBackToTheServerTimeZone() throws Exception {
    when(userProfileHandler.findUserProfileByName(USERNAME)).thenThrow(new IllegalStateException("Store is down"));
    assertNull(userTimeZoneService.getUserTimeZone(USERNAME));
  }

  @Test
  public void testSaveUserTimeZone() throws Exception {
    when(userProfileHandler.findUserProfileByName(USERNAME)).thenReturn(userProfile);

    userTimeZoneService.saveUserTimeZone(USERNAME, TIME_ZONE);

    verify(userProfile).setAttribute(Profile.USER_TIME_ZONE, TIME_ZONE);
    verify(userProfileHandler).saveUserProfile(userProfile, true);
    // Whoever keeps a copy of the timezone, like the digest work list,
    // refreshes it on this event: a profile listener never fires for it
    verify(listenerService).broadcast(UserTimeZoneService.USER_TIME_ZONE_SAVED_EVENT, USERNAME, TIME_ZONE);
  }

  @Test
  public void testSaveCreatesTheProfileOfANeverSeenUser() throws Exception {
    when(userProfileHandler.findUserProfileByName(USERNAME)).thenReturn(null);
    when(userProfileHandler.createUserProfileInstance(USERNAME)).thenReturn(userProfile);

    userTimeZoneService.saveUserTimeZone(USERNAME, TIME_ZONE);

    verify(userProfile).setAttribute(Profile.USER_TIME_ZONE, TIME_ZONE);
    verify(userProfileHandler).saveUserProfile(userProfile, true);
  }

  @Test
  public void testAnUnknownTimeZoneIsRefused() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> userTimeZoneService.saveUserTimeZone(USERNAME, "Mars/Olympus"));
    assertThrows(IllegalArgumentException.class, () -> userTimeZoneService.saveUserTimeZone(USERNAME, null));
    assertThrows(IllegalArgumentException.class, () -> userTimeZoneService.saveUserTimeZone(null, TIME_ZONE));
    verify(userProfileHandler, never()).saveUserProfile(any(), any(Boolean.class).booleanValue());
    verify(listenerService, never()).broadcast(any(String.class), any(), any());
  }

  @Test
  public void testABroadcastFailureDoesNotUndoTheSave() throws Exception {
    when(userProfileHandler.findUserProfileByName(USERNAME)).thenReturn(userProfile);
    doBroadcastFailure();

    userTimeZoneService.saveUserTimeZone(USERNAME, TIME_ZONE);

    verify(userProfileHandler).saveUserProfile(userProfile, true);
  }

  private void doBroadcastFailure() throws Exception {
    org.mockito.Mockito.doThrow(new IllegalStateException("Listener failure"))
                       .when(listenerService)
                       .broadcast(any(String.class), any(), any());
  }

}
