/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.service;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.model.GettingStartedStep;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class GettingStartedServiceTest {

  private static final String          USERNAME_TEST = "test";

  private static GettingStartedService gettingStartedService;

  private static IdentityManager       identityManager;

  private static SpaceService          spaceService;

  private static RelationshipManager   relationshipManager;

  private static ActivityManager       activityService;

  @BeforeClass
  public static void beforeClass() {
    identityManager = Mockito.mock(IdentityManager.class);
    spaceService = Mockito.mock(SpaceService.class);
    relationshipManager = Mockito.mock(RelationshipManager.class);
    activityService = Mockito.mock(ActivityManager.class);
    gettingStartedService = new GettingStartedService(identityManager, spaceService, relationshipManager, activityService);
  }

  @Before
  public void setup() {
    Mockito.reset(identityManager, spaceService, relationshipManager, activityService);
  }

  @Test
  public void testGetUserStepsWithNullUserName() {
    try {
      gettingStartedService.getUserSteps(null);
      fail("Should throw exception with null username");
    } catch (IllegalArgumentException e) {

    }
  }

  @Test
  public void testGetUserStepsWithNotExistUserName() {
    List<GettingStartedStep> gettingStartedUserSteps = gettingStartedService.getUserSteps(USERNAME_TEST);
    assertNotNull(gettingStartedUserSteps);
    assertEquals(4, gettingStartedUserSteps.size());
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.AVATAR_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.AVATAR_STEP_KEY)
                                          && !userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.SPACES_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.SPACES_STEP_KEY)
                                          && !userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.ACTIVITIES_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.ACTIVITIES_STEP_KEY)
                                          && !userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.CONTACTS_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.CONTACTS_STEP_KEY)
                                          && !userStep.getStatus()));
  }

  @Test
  public void testGetUserStepsWithExistUserNameHasAvatar() {
    Identity identity = Mockito.mock(Identity.class);
    Profile profile = new Profile(identity);
    Mockito.when(identityManager.getOrCreateIdentity(Mockito.eq(OrganizationIdentityProvider.NAME), Mockito.eq(USERNAME_TEST)))
           .thenReturn(identity);
    profile.setAvatarLastUpdated(1234567L);
    Mockito.when(identity.getProfile()).thenReturn(profile);
    List<GettingStartedStep> gettingStartedUserSteps = gettingStartedService.getUserSteps(USERNAME_TEST);
    assertNotNull(gettingStartedUserSteps);
    assertEquals(4, gettingStartedUserSteps.size());
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.AVATAR_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.AVATAR_STEP_KEY)
                                          && userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.SPACES_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.SPACES_STEP_KEY)
                                          && !userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.ACTIVITIES_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.ACTIVITIES_STEP_KEY)
                                          && !userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.CONTACTS_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.CONTACTS_STEP_KEY)
                                          && !userStep.getStatus()));
  }

  @Test
  public void testGetUserStepsWithExistUserNameHasContacts() throws Exception {
    Identity identity = Mockito.mock(Identity.class);
    Mockito.when(identityManager.getOrCreateIdentity(Mockito.eq(OrganizationIdentityProvider.NAME), Mockito.eq(USERNAME_TEST)))
           .thenReturn(identity);
    ListAccess<Identity> listConnections = Mockito.mock(ListAccess.class);
    Mockito.when(listConnections.getSize()).thenReturn(1);
    Mockito.when(relationshipManager.getConnections(identity)).thenReturn(listConnections);
    List<GettingStartedStep> gettingStartedUserSteps = gettingStartedService.getUserSteps(USERNAME_TEST);
    assertNotNull(gettingStartedUserSteps);
    assertEquals(4, gettingStartedUserSteps.size());
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.AVATAR_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.AVATAR_STEP_KEY)
                                          && !userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.SPACES_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.SPACES_STEP_KEY)
                                          && !userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.ACTIVITIES_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.ACTIVITIES_STEP_KEY)
                                          && !userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.CONTACTS_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.CONTACTS_STEP_KEY)
                                          && userStep.getStatus()));
  }

  @Test
  public void testGetUserStepsWithExistUserNameHasSpaces() throws Exception {
    ListAccess<Space> accessibleSpaces = Mockito.mock(ListAccess.class);
    Mockito.when(accessibleSpaces.getSize()).thenReturn(1);
    Mockito.when(spaceService.getAccessibleSpacesWithListAccess(USERNAME_TEST)).thenReturn(accessibleSpaces);
    List<GettingStartedStep> gettingStartedUserSteps = gettingStartedService.getUserSteps(USERNAME_TEST);
    assertNotNull(gettingStartedUserSteps);
    assertEquals(4, gettingStartedUserSteps.size());
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.AVATAR_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.AVATAR_STEP_KEY)
                                          && !userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.SPACES_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.SPACES_STEP_KEY)
                                          && userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.ACTIVITIES_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.ACTIVITIES_STEP_KEY)
                                          && !userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.CONTACTS_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.CONTACTS_STEP_KEY)
                                          && !userStep.getStatus()));

  }

  @Test
  public void testGetUserStepsWithExistUserNameHasActivities() throws Exception {

    Identity identity = Mockito.mock(Identity.class);
    Mockito.when(identityManager.getOrCreateIdentity(Mockito.eq(OrganizationIdentityProvider.NAME), Mockito.eq(USERNAME_TEST)))
           .thenReturn(identity);
    /*----------Avatar Step is True---------*/
    Profile profile = new Profile(identity);
    profile.setAvatarLastUpdated(1234567L);
    Mockito.when(identity.getProfile()).thenReturn(profile);

    /*----------Connexion Step is true ---------*/
    ListAccess<Identity> listConnections = Mockito.mock(ListAccess.class);
    Mockito.when(listConnections.getSize()).thenReturn(1);
    Mockito.when(relationshipManager.getConnections(identity)).thenReturn(listConnections);

    /*----------Space Step is true---------*/
    ListAccess<Space> accessibleSpaces = Mockito.mock(ListAccess.class);
    Mockito.when(accessibleSpaces.getSize()).thenReturn(1);
    Mockito.when(spaceService.getAccessibleSpacesWithListAccess(USERNAME_TEST)).thenReturn(accessibleSpaces);

    /*----------Activities Step is true---------*/
    RealtimeListAccess activities = Mockito.mock(RealtimeListAccess.class);
    Mockito.when(activities.getSize()).thenReturn(1);
    Mockito.when(activityService.getActivitiesWithListAccess(identity)).thenReturn(activities);

    List<GettingStartedStep> gettingStartedUserSteps = gettingStartedService.getUserSteps(USERNAME_TEST);
    assertNotNull(gettingStartedUserSteps);
    assertEquals(4, gettingStartedUserSteps.size());
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.AVATAR_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.AVATAR_STEP_KEY)
                                          && userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.SPACES_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.SPACES_STEP_KEY)
                                          && userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.ACTIVITIES_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.ACTIVITIES_STEP_KEY)
                                          && userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.CONTACTS_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.CONTACTS_STEP_KEY)
                                          && userStep.getStatus()));
  }

  @Test
  public void testGetUserStepsWithExistUserNameAllStepsAreFalse() throws Exception {
    Identity identity = Mockito.mock(Identity.class);
    Mockito.when(identityManager.getOrCreateIdentity(Mockito.eq(OrganizationIdentityProvider.NAME), Mockito.eq(USERNAME_TEST)))
           .thenReturn(identity);

    Profile profile = new Profile(identity);
    Mockito.when(identity.getProfile()).thenReturn(profile);

    ListAccess<Identity> listConnections = Mockito.mock(ListAccess.class);
    Mockito.when(listConnections.getSize()).thenReturn(0);
    Mockito.when(relationshipManager.getConnections(identity)).thenReturn(listConnections);

    ListAccess<Space> accessibleSpaces = Mockito.mock(ListAccess.class);
    Mockito.when(accessibleSpaces.getSize()).thenReturn(0);
    Mockito.when(spaceService.getAccessibleSpacesWithListAccess(USERNAME_TEST)).thenReturn(accessibleSpaces);

    RealtimeListAccess activities = Mockito.mock(RealtimeListAccess.class);
    Mockito.when(activities.getSize()).thenReturn(0);
    Mockito.when(activityService.getActivitiesWithListAccess(identity)).thenReturn(activities);

    List<GettingStartedStep> gettingStartedUserSteps = gettingStartedService.getUserSteps(USERNAME_TEST);
    assertNotNull(gettingStartedUserSteps);
    assertEquals(4, gettingStartedUserSteps.size());
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.AVATAR_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.AVATAR_STEP_KEY)
                                          && !userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.SPACES_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.SPACES_STEP_KEY)
                                          && !userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.ACTIVITIES_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.ACTIVITIES_STEP_KEY)
                                          && !userStep.getStatus()));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.CONTACTS_STEP_KEY)));
    assertTrue(gettingStartedUserSteps.stream()
                                      .anyMatch(userStep -> StringUtils.equals(userStep.getName(),
                                                                               gettingStartedService.CONTACTS_STEP_KEY)
                                          && !userStep.getStatus()));

  }

}
