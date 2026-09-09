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
package io.meeds.social.notification.rest.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.entity.ProfileEntity;
import org.exoplatform.social.rest.entity.SpaceEntity;

import io.meeds.social.notification.rest.model.WebNotificationListRestEntity;
import io.meeds.social.notification.rest.model.WebNotificationRestEntity;

/**
 * The REST shape of a web notification: who sent it (found in the
 * notification itself or in one of the parameter keys the plugins use), the
 * space it belongs to (by id, by pretty name, or through its activity), and
 * the message built by the service.
 */
@RunWith(MockitoJUnitRunner.class)
public class WebNotificationRestEntityBuilderTest {

  private static final String         PLUGIN_ID = "ActivityCommentPlugin";

  @Mock
  private WebNotificationService      webNotificationService;

  @Mock
  private IdentityManager             identityManager;

  @Mock
  private SpaceService                spaceService;

  @Mock
  private ActivityManager             activityManager;

  @Mock
  private ProfileEntity               profileEntity;

  @Mock
  private SpaceEntity                 spaceEntity;

  private Identity                    identity;

  private Space                       space;

  private MockedStatic<EntityBuilder> entityBuilder;

  private MockedStatic<Utils>         utils;

  @Before
  public void setUp() {
    identity = new Identity("7");
    identity.setProfile(new Profile(identity));
    space = new Space();
    space.setId("5");
    entityBuilder = mockStatic(EntityBuilder.class);
    entityBuilder.when(() -> EntityBuilder.buildEntityProfile(identity.getProfile(), null)).thenReturn(profileEntity);
    entityBuilder.when(() -> EntityBuilder.buildEntityFromSpace(space, null, null, null)).thenReturn(spaceEntity);
    utils = mockStatic(Utils.class);
    utils.when(Utils::getActivityManager).thenReturn(activityManager);
  }

  @After
  public void tearDown() {
    entityBuilder.close();
    utils.close();
  }

  @Test
  public void testSenderFromTheNotificationAndSpaceById() {
    NotificationInfo notification = notification().setFrom("john").with("spaceId", "5").setRead(true);
    notification.setLastModifiedDate(1234L);
    when(identityManager.getOrCreateUserIdentity("john")).thenReturn(identity);
    when(spaceService.getSpaceById("5")).thenReturn(space);
    when(webNotificationService.getNotificationMessage(notification, true)).thenReturn("<b>hi</b>");

    WebNotificationRestEntity entity = WebNotificationRestEntityBuilder.toRestEntity(webNotificationService,
                                                                                     identityManager,
                                                                                     spaceService,
                                                                                     notification,
                                                                                     true);

    assertEquals("12", entity.getId());
    assertEquals("A title", entity.getTitle());
    assertEquals(PLUGIN_ID, entity.getPlugin());
    assertSame(profileEntity, entity.getFrom());
    assertSame(spaceEntity, entity.getSpace());
    assertEquals("<b>hi</b>", entity.getHtml());
    assertEquals("5", entity.getParameters().get("spaceId"));
    assertTrue(entity.isRead());
    assertEquals(1234L, entity.getCreated());
  }

  @Test
  public void testSenderFromAParameterKeyAndSpaceByPrettyName() {
    NotificationInfo notification = notification().with("poster", "mary").with("prettyName", "dev_team");
    when(identityManager.getOrCreateUserIdentity("mary")).thenReturn(identity);
    when(spaceService.getSpaceByPrettyName("dev_team")).thenReturn(space);

    WebNotificationRestEntity entity = toRestEntity(notification);

    assertSame(profileEntity, entity.getFrom());
    assertSame(spaceEntity, entity.getSpace());
    verify(spaceService, never()).getSpaceById(anyString());
  }

  @Test
  public void testSenderByNumericIdentityIdWhenNoUserHasThatName() {
    NotificationInfo notification = notification().with("creatorId", "42");
    when(identityManager.getOrCreateUserIdentity("42")).thenReturn(null);
    when(identityManager.getIdentity("42")).thenReturn(identity);

    WebNotificationRestEntity entity = toRestEntity(notification);

    assertSame(profileEntity, entity.getFrom());
    assertNull(entity.getSpace());
  }

  @Test
  public void testUnknownSenderNameGivesNoSender() {
    NotificationInfo notification = notification().with("sender", "ghost");
    when(identityManager.getOrCreateUserIdentity("ghost")).thenReturn(null);

    WebNotificationRestEntity entity = toRestEntity(notification);

    assertNull(entity.getFrom());
    verify(identityManager, never()).getIdentity(anyString());
  }

  @Test
  public void testNoSenderKeyAtAllAsksNobody() {
    WebNotificationRestEntity entity = toRestEntity(notification());

    assertNull(entity.getFrom());
    assertNull(entity.getSpace());
    verify(identityManager, never()).getOrCreateUserIdentity(anyString());
    verify(identityManager, never()).getIdentity(anyString());
  }

  @Test
  public void testSpaceFoundThroughTheActivityStream() {
    NotificationInfo notification = notification().with("activityId", "77");
    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    ActivityStream stream = mock(ActivityStream.class);
    when(activityManager.getActivity("77")).thenReturn(activity);
    when(activity.getActivityStream()).thenReturn(stream);
    when(stream.isSpace()).thenReturn(true);
    when(activity.getSpaceId()).thenReturn("5");
    when(spaceService.getSpaceById("5")).thenReturn(space);

    assertSame(spaceEntity, toRestEntity(notification).getSpace());
  }

  @Test
  public void testActivityOutsideASpaceGivesNoSpace() {
    NotificationInfo notification = notification().with("activityId", "77");
    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    ActivityStream stream = mock(ActivityStream.class);
    when(activityManager.getActivity("77")).thenReturn(activity);
    when(activity.getActivityStream()).thenReturn(stream);
    when(stream.isSpace()).thenReturn(false);

    assertNull(toRestEntity(notification).getSpace());
    verify(spaceService, never()).getSpaceById(anyString());
  }

  @Test
  public void testDeletedActivityGivesNoSpace() {
    NotificationInfo notification = notification().with("activityId", "77");
    when(activityManager.getActivity("77")).thenReturn(null);

    assertNull(toRestEntity(notification).getSpace());
  }

  @Test
  public void testListCarriesEveryNotificationAndThePaging() {
    NotificationInfo first = notification().setId("1").setFrom("john");
    NotificationInfo second = notification().setId("2");
    when(identityManager.getOrCreateUserIdentity("john")).thenReturn(identity);
    when(webNotificationService.getNotificationMessage(any(NotificationInfo.class), anyBoolean())).thenReturn("m");

    WebNotificationListRestEntity list = WebNotificationRestEntityBuilder.toRestEntity(webNotificationService,
                                                                                       identityManager,
                                                                                       spaceService,
                                                                                       List.of(first, second),
                                                                                       false,
                                                                                       3,
                                                                                       Map.of(PLUGIN_ID, 3),
                                                                                       10,
                                                                                       20);

    assertEquals(2, list.getNotifications().size());
    assertEquals("1", list.getNotifications().get(0).getId());
    assertSame(profileEntity, list.getNotifications().get(0).getFrom());
    assertEquals("2", list.getNotifications().get(1).getId());
    assertNull(list.getNotifications().get(1).getFrom());
    assertFalse(list.getNotifications().get(1).isRead());
    assertEquals(3, list.getBadge());
    assertEquals(Integer.valueOf(3), list.getBadgesByPlugin().get(PLUGIN_ID));
    assertEquals(10, list.getOffset());
    assertEquals(20, list.getLimit());
  }

  private WebNotificationRestEntity toRestEntity(NotificationInfo notification) {
    return WebNotificationRestEntityBuilder.toRestEntity(webNotificationService,
                                                         identityManager,
                                                         spaceService,
                                                         notification,
                                                         false);
  }

  private NotificationInfo notification() {
    return NotificationInfo.instance().setId("12").key(PLUGIN_ID).setTitle("A title");
  }

}
