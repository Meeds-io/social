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
package org.exoplatform.social.rest.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;
import org.exoplatform.social.rest.entity.MetadataItemEntity;

import io.meeds.social.report.service.ActivityReportService;

/**
 * Spec-mandated privacy regression test: no report ever appears as published
 * metadata in a payload built through the real entity-building path — for the
 * reporter themselves as much as for any other caller. A mocked metadata
 * service could not catch this exclusion silently ceasing to apply.
 */
public class EntityBuilderReportsTest {

  private static final long VIEWER_IDENTITY_ID = 999L;

  private IdentityManager   identityManager;

  private ActivityManager   activityManager;

  @Before
  public void setUp() throws Exception {
    identityManager = mock(IdentityManager.class);
    activityManager = mock(ActivityManager.class);
    setStaticField("identityManager", identityManager);
    setStaticField("activityManager", activityManager);
  }

  @After
  public void tearDown() throws Exception {
    setStaticField("identityManager", null);
    setStaticField("activityManager", null);
  }

  @Test
  public void testReportItemsNeverPublishedEvenToTheirReporter() {
    ExoSocialActivity activity = spaceActivity();
    Map<String, List<MetadataItem>> metadatas = new HashMap<>();
    metadatas.put(ActivityReportService.METADATA_TYPE_NAME,
                  List.of(reportItem(VIEWER_IDENTITY_ID), reportItem(555L)));
    metadatas.put("favorites", List.of(favoriteItem(VIEWER_IDENTITY_ID)));
    activity.setMetadatas(metadatas);

    Map<String, List<MetadataItemEntity>> published = EntityBuilder.retrieveMetadataItems(activity, viewerIdentity());

    assertNotNull(published);
    assertFalse("Report items must never be published as metadata, the reporter's own included",
                published.containsKey(ActivityReportService.METADATA_TYPE_NAME));
    assertTrue("Other metadata types must keep being published", published.containsKey("favorites"));
  }

  @Test
  public void testHasReportedOnlyForCallerActiveReport() throws Exception {
    ExoSocialActivity activity = spaceActivity();
    Map<String, List<MetadataItem>> metadatas = new HashMap<>();

    metadatas.put(ActivityReportService.METADATA_TYPE_NAME, List.of(reportItem(555L)));
    activity.setMetadatas(metadatas);
    assertFalse("Another reporter's item must not mark the caller as having reported",
                hasReportedActivity(activity, viewerIdentity()));

    metadatas.put(ActivityReportService.METADATA_TYPE_NAME, List.of(staleReportItem(VIEWER_IDENTITY_ID)));
    assertFalse("A stale report must not mark the caller as having reported",
                hasReportedActivity(activity, viewerIdentity()));

    metadatas.put(ActivityReportService.METADATA_TYPE_NAME, List.of(reportItem(VIEWER_IDENTITY_ID)));
    assertTrue("The caller's own active report must mark them as having reported",
               hasReportedActivity(activity, viewerIdentity()));
  }

  @Test
  public void testCanReportOnlyOnSpaceFeedContentAndNeverForTheAuthor() throws Exception {
    ExoSocialActivity activity = spaceActivity();
    assertTrue(canReportActivity(activity, viewerIdentity()));

    Identity author = mock(Identity.class);
    lenient().when(author.getId()).thenReturn("111");
    assertFalse("The author must never be offered to report their own content",
                canReportActivity(activity, author));

    Identity userStreamOwner = mock(Identity.class);
    lenient().when(userStreamOwner.isSpace()).thenReturn(false);
    when(identityManager.getOrCreateUserIdentity(anyString())).thenReturn(userStreamOwner);
    assertFalse("Personal-stream content must not be reportable",
                canReportActivity(activity, viewerIdentity()));
  }

  private boolean hasReportedActivity(ExoSocialActivity activity, Identity viewer) throws Exception {
    Method method = EntityBuilder.class.getDeclaredMethod("hasReportedActivity", ExoSocialActivity.class, Identity.class);
    method.setAccessible(true); // NOSONAR
    return (boolean) method.invoke(null, activity, viewer);
  }

  private boolean canReportActivity(ExoSocialActivity activity, Identity viewer) throws Exception {
    Method method = EntityBuilder.class.getDeclaredMethod("canReportActivity", ExoSocialActivity.class, Identity.class);
    method.setAccessible(true); // NOSONAR
    return (boolean) method.invoke(null, activity, viewer);
  }

  private ExoSocialActivity spaceActivity() {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setId("55");
    activity.setPosterId("111");
    activity.setStreamOwner("testspace");
    Identity spaceIdentity = mock(Identity.class);
    lenient().when(spaceIdentity.getId()).thenReturn("5");
    lenient().when(spaceIdentity.isSpace()).thenReturn(true);
    lenient().when(identityManager.getOrCreateUserIdentity(anyString())).thenReturn(spaceIdentity);
    return activity;
  }

  private Identity viewerIdentity() {
    Identity viewer = mock(Identity.class);
    lenient().when(viewer.getId()).thenReturn(String.valueOf(VIEWER_IDENTITY_ID));
    return viewer;
  }

  private MetadataItem reportItem(long creatorId) {
    return metadataItem(ActivityReportService.METADATA_TYPE_NAME, creatorId, creatorId, null);
  }

  private MetadataItem staleReportItem(long creatorId) {
    Map<String, String> properties = new HashMap<>();
    properties.put(ActivityReportService.STATUS_PROPERTY, ActivityReportService.STATUS_STALE);
    return metadataItem(ActivityReportService.METADATA_TYPE_NAME, creatorId, creatorId, properties);
  }

  private MetadataItem favoriteItem(long creatorId) {
    return metadataItem("favorites", creatorId, creatorId, null);
  }

  private MetadataItem metadataItem(String type, long creatorId, long audienceId, Map<String, String> properties) {
    Metadata metadata = new Metadata();
    metadata.setName(String.valueOf(creatorId));
    metadata.setAudienceId(audienceId);
    metadata.setType(new MetadataType(89471L, type));
    return new MetadataItem(creatorId,
                            metadata,
                            new MetadataObject("activity", "55"),
                            creatorId,
                            System.currentTimeMillis(),
                            properties);
  }

  private void setStaticField(String name, Object value) throws Exception {
    Field field = EntityBuilder.class.getDeclaredField(name);
    field.setAccessible(true); // NOSONAR
    field.set(null, value);
  }

}
