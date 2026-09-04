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
package io.meeds.social.report.processor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;

import io.meeds.social.report.service.ActivityReportService;

@RunWith(MockitoJUnitRunner.class)
public class ReportActivityProcessorTest {

  @Mock
  private MetadataService         metadataService;

  private ReportActivityProcessor processor;

  @Before
  public void setUp() {
    processor = new ReportActivityProcessor(metadataService, new InitParams());
  }

  @Test
  public void testSkipsNonRedirectedActivities() {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setId("55");

    processor.processActivity(activity);
    processor.processActivity(null);

    verifyNoInteractions(metadataService);
  }

  @Test
  public void testLoadsActivityAnchoredReportsForRedirectedActivities() {
    ExoSocialActivity activity = newsActivity();
    MetadataItem reportItem = new MetadataItem();
    when(metadataService.getMetadataItemsByMetadataTypeAndObject(eq(ActivityReportService.METADATA_TYPE_NAME),
                                                                 any())).thenReturn(List.of(reportItem));

    processor.processActivity(activity);

    assertNotNull(activity.getMetadatas());
    assertTrue(activity.getMetadatas().containsKey(ActivityReportService.METADATA_TYPE_NAME));
  }

  @Test
  public void testKeepsMetadataMapUntouchedWithoutReports() {
    ExoSocialActivity activity = newsActivity();
    when(metadataService.getMetadataItemsByMetadataTypeAndObject(eq(ActivityReportService.METADATA_TYPE_NAME),
                                                                 any())).thenReturn(List.of());

    processor.processActivity(activity);

    assertTrue(activity.getMetadatas() == null || !activity.getMetadatas().containsKey(ActivityReportService.METADATA_TYPE_NAME));
  }

  private ExoSocialActivity newsActivity() {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setId("55");
    Map<String, String> templateParams = new HashMap<>();
    templateParams.put("metadataObjectType", "news");
    templateParams.put("metadataObjectId", "8");
    activity.setTemplateParams(templateParams);
    return activity;
  }

}
