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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;

import io.meeds.social.report.service.ActivityReportService;

/**
 * Reports always anchor on the activity/comment itself. For a content-backed
 * activity (e.g. a news post), the generic {@link MetadataService} processor
 * loads metadata by the redirected content object, so the activity-anchored
 * report items would be missing from the cached activity's metadata map — this
 * processor loads them, keeping the derived report flags free of any dedicated
 * read-path query.
 */
public class ReportActivityProcessor extends BaseActivityProcessorPlugin {

  private MetadataService metadataService;

  public ReportActivityProcessor(MetadataService metadataService, InitParams params) {
    super(params);
    this.metadataService = metadataService;
  }

  @Override
  public void processActivity(ExoSocialActivity activity) {
    if (activity == null || !activity.hasSpecificMetadataObject()) {
      // Non-redirected activities/comments: the generic metadata processor
      // already loaded their report items
      return;
    }
    long spaceId = StringUtils.isBlank(activity.getSpaceId()) ? 0 : Long.parseLong(activity.getSpaceId());
    MetadataObject reportObject = new MetadataObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                                     activity.getId(),
                                                     activity.getParentId(),
                                                     spaceId);
    List<MetadataItem> reportItems =
                                   metadataService.getMetadataItemsByMetadataTypeAndObject(ActivityReportService.METADATA_TYPE_NAME,
                                                                                           reportObject);
    if (CollectionUtils.isNotEmpty(reportItems)) {
      Map<String, List<MetadataItem>> metadatas = activity.getMetadatas();
      if (metadatas == null) {
        metadatas = new HashMap<>();
        activity.setMetadatas(metadatas);
      }
      metadatas.put(ActivityReportService.METADATA_TYPE_NAME, reportItems);
    }
  }

}
