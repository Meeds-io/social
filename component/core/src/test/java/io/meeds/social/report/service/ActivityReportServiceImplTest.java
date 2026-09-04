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
package io.meeds.social.report.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.metadata.model.MetadataType;

import io.meeds.social.report.model.ActivityReport;

@RunWith(MockitoJUnitRunner.class)
public class ActivityReportServiceImplTest {

  private static final String                          ACTIVITY_ID    = "55";

  private static final long                            REPORTER_ID    = 999L;

  private static final String                          REPORTER_USER  = "john";

  private static final String                          REASON         = "spam";

  @Mock
  private ActivityManager                              activityManager;

  @Mock
  private IdentityManager                              identityManager;

  @Mock
  private MetadataService                              metadataService;

  @Mock
  private ListenerService                              listenerService;

  @Mock
  private ExoSocialActivity                            activity;

  @Mock
  private Identity                                     reporterIdentity;

  @Mock
  private Identity                                     spaceIdentity;

  @InjectMocks
  private ActivityReportServiceImpl                    activityReportService;

  private org.exoplatform.services.security.Identity  aclIdentity    = new org.exoplatform.services.security.Identity(REPORTER_USER);

  @Before
  public void setUp() {
    lenient().when(activityManager.getActivity(ACTIVITY_ID)).thenReturn(activity);
    lenient().when(activityManager.isActivityViewable(activity, aclIdentity)).thenReturn(true);
    lenient().when(activity.getId()).thenReturn(ACTIVITY_ID);
    lenient().when(activity.getPosterId()).thenReturn("111");
    lenient().when(activity.getSpaceId()).thenReturn("8");
    lenient().when(identityManager.getOrCreateUserIdentity(REPORTER_USER)).thenReturn(reporterIdentity);
    lenient().when(reporterIdentity.getId()).thenReturn(String.valueOf(REPORTER_ID));
    lenient().when(activityManager.getActivityStreamOwnerIdentity(ACTIVITY_ID)).thenReturn(spaceIdentity);
    lenient().when(spaceIdentity.isSpace()).thenReturn(true);
    lenient().when(spaceIdentity.getId()).thenReturn("5");
  }

  @Test
  public void testReportActivityAnchorsOnTheActivityObject() throws Exception {
    when(metadataService.getMetadataItemsByMetadataAndObject(any(), any())).thenReturn(List.of());

    ActivityReport report = activityReportService.reportActivity(ACTIVITY_ID, REASON, aclIdentity);

    assertNotNull(report);
    ArgumentCaptor<MetadataObject> objectCaptor = ArgumentCaptor.forClass(MetadataObject.class);
    ArgumentCaptor<MetadataKey> keyCaptor = ArgumentCaptor.forClass(MetadataKey.class);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, String>> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
    verify(metadataService).createMetadataItem(objectCaptor.capture(),
                                               keyCaptor.capture(),
                                               propertiesCaptor.capture(),
                                               eq(REPORTER_ID));
    assertEquals("Reports must anchor on the activity object itself, never on a redirected content object",
                 "activity",
                 objectCaptor.getValue().getType());
    assertEquals(ACTIVITY_ID, objectCaptor.getValue().getId());
    assertEquals(ActivityReportService.METADATA_TYPE_NAME, keyCaptor.getValue().getType());
    assertEquals(String.valueOf(REPORTER_ID), keyCaptor.getValue().getName());
    assertEquals(REPORTER_ID, keyCaptor.getValue().getAudienceId());
    assertEquals(REASON, propertiesCaptor.getValue().get(ActivityReportService.REASON_PROPERTY));
    assertEquals(ActivityReportService.STATUS_ACTIVE, propertiesCaptor.getValue().get(ActivityReportService.STATUS_PROPERTY));
    verify(listenerService).broadcast(eq(ActivityReportService.EVENT_ACTIVITY_REPORTED), any(ActivityReport.class), eq(REPORTER_ID));
  }

  @Test
  public void testReportActivityRejectsSameReporterDoubleSubmission() throws Exception {
    when(metadataService.getMetadataItemsByMetadataAndObject(any(), any())).thenReturn(List.of(reportItem(null)));

    assertThrows(ObjectAlreadyExistsException.class,
                 () -> activityReportService.reportActivity(ACTIVITY_ID, REASON, aclIdentity));
    verify(metadataService, never()).createMetadataItem(any(), any(), any(Map.class), anyLong());
    verify(metadataService, never()).updateMetadataItem(any(), anyLong());
  }

  @Test
  public void testReportActivityReactivatesStaleReportInsteadOfDuplicating() throws Exception {
    Map<String, String> staleProperties = new HashMap<>();
    staleProperties.put(ActivityReportService.STATUS_PROPERTY, ActivityReportService.STATUS_STALE);
    MetadataItem staleItem = reportItem(staleProperties);
    when(metadataService.getMetadataItemsByMetadataAndObject(any(), any())).thenReturn(List.of(staleItem));

    activityReportService.reportActivity(ACTIVITY_ID, REASON, aclIdentity);

    verify(metadataService, never()).createMetadataItem(any(), any(), any(Map.class), anyLong());
    verify(metadataService).updateMetadataItem(staleItem, REPORTER_ID);
    assertEquals(ActivityReportService.STATUS_ACTIVE,
                 staleItem.getProperties().get(ActivityReportService.STATUS_PROPERTY));
    assertEquals(REASON, staleItem.getProperties().get(ActivityReportService.REASON_PROPERTY));
  }

  @Test
  public void testReportActivityGuardsInContractOrder() {
    when(activityManager.getActivity("missing")).thenReturn(null);
    assertThrows(ObjectNotFoundException.class,
                 () -> activityReportService.reportActivity("missing", REASON, aclIdentity));

    when(activityManager.isActivityViewable(activity, aclIdentity)).thenReturn(false);
    assertThrows(IllegalAccessException.class,
                 () -> activityReportService.reportActivity(ACTIVITY_ID, REASON, aclIdentity));

    when(activityManager.isActivityViewable(activity, aclIdentity)).thenReturn(true);
    when(activity.getPosterId()).thenReturn(String.valueOf(REPORTER_ID));
    assertThrows("The author must never be able to report their own content, enforced server-side",
                 IllegalAccessException.class,
                 () -> activityReportService.reportActivity(ACTIVITY_ID, REASON, aclIdentity));

    when(activity.getPosterId()).thenReturn("111");
    when(spaceIdentity.isSpace()).thenReturn(false);
    assertThrows("Personal-stream content must be rejected server-side",
                 IllegalArgumentException.class,
                 () -> activityReportService.reportActivity(ACTIVITY_ID, REASON, aclIdentity));

    when(spaceIdentity.isSpace()).thenReturn(true);
    assertThrows("An unknown reason must be rejected",
                 IllegalArgumentException.class,
                 () -> activityReportService.reportActivity(ACTIVITY_ID, "notAReason", aclIdentity));
  }

  @Test
  public void testMarkReportsStaleFlipsEveryActiveReportAcrossReporters() {
    MetadataItem activeReport1 = reportItem(1L, null);
    Map<String, String> staleProperties = new HashMap<>();
    staleProperties.put(ActivityReportService.STATUS_PROPERTY, ActivityReportService.STATUS_STALE);
    MetadataItem alreadyStale = reportItem(2L, staleProperties);
    Map<String, String> activeProperties = new HashMap<>();
    activeProperties.put(ActivityReportService.STATUS_PROPERTY, ActivityReportService.STATUS_ACTIVE);
    activeProperties.put(ActivityReportService.REASON_PROPERTY, REASON);
    MetadataItem activeReport2 = reportItem(3L, activeProperties);
    when(metadataService.getMetadataItemsByMetadataTypeAndObject(eq(ActivityReportService.METADATA_TYPE_NAME), any()))
                                                                                                                      .thenReturn(List.of(activeReport1,
                                                                                                                                          alreadyStale,
                                                                                                                                          activeReport2));

    activityReportService.markReportsStale(activity);

    ArgumentCaptor<MetadataItem> updatedItems = ArgumentCaptor.forClass(MetadataItem.class);
    verify(metadataService, times(2)).updateMetadataItem(updatedItems.capture(), anyLong());
    List<Long> updatedIds = updatedItems.getAllValues().stream().map(MetadataItem::getId).toList();
    assertEquals("Every active report must be flipped, across all reporters, and only those",
                 List.of(1L, 3L),
                 updatedIds);
    assertEquals(ActivityReportService.STATUS_STALE,
                 activeReport1.getProperties().get(ActivityReportService.STATUS_PROPERTY));
    assertEquals(ActivityReportService.STATUS_STALE,
                 activeReport2.getProperties().get(ActivityReportService.STATUS_PROPERTY));
    assertEquals("The original reason must be kept for moderation audit",
                 REASON,
                 activeReport2.getProperties().get(ActivityReportService.REASON_PROPERTY));
  }

  private MetadataItem reportItem(Map<String, String> properties) {
    return reportItem(1L, properties);
  }

  private MetadataItem reportItem(long itemId, Map<String, String> properties) {
    Metadata metadata = new Metadata();
    metadata.setName(String.valueOf(REPORTER_ID));
    metadata.setAudienceId(REPORTER_ID);
    metadata.setType(new MetadataType(89471L, ActivityReportService.METADATA_TYPE_NAME));
    MetadataItem item = new MetadataItem(itemId,
                                         metadata,
                                         new MetadataObject("activity", ACTIVITY_ID),
                                         REPORTER_ID,
                                         System.currentTimeMillis(),
                                         properties);
    return item;
  }

}
