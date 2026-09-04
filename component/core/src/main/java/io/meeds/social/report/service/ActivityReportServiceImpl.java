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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;

import io.meeds.social.report.model.ActivityReport;

@Service
public class ActivityReportServiceImpl implements ActivityReportService {

  private static final Log LOG = ExoLogger.getLogger(ActivityReportServiceImpl.class);

  @Autowired
  private ActivityManager  activityManager;

  @Autowired
  private IdentityManager  identityManager;

  @Autowired
  private MetadataService  metadataService;

  @Autowired
  private ListenerService  listenerService;

  @Override
  public ActivityReport reportActivity(String activityId,
                                       String reason,
                                       org.exoplatform.services.security.Identity viewerAclIdentity) throws ObjectNotFoundException,
                                                                                                     IllegalAccessException,
                                                                                                     ObjectAlreadyExistsException {
    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null) {
      throw new ObjectNotFoundException("Activity " + activityId + " not found");
    }
    if (viewerAclIdentity == null || !activityManager.isActivityViewable(activity, viewerAclIdentity)) {
      throw new IllegalAccessException("User isn't allowed to view activity " + activityId);
    }
    Identity reporterIdentity = identityManager.getOrCreateUserIdentity(viewerAclIdentity.getUserId());
    if (reporterIdentity == null || StringUtils.equals(activity.getPosterId(), reporterIdentity.getId())) {
      throw new IllegalAccessException("The author of an activity can't report their own content");
    }
    Identity streamOwner = activityManager.getActivityStreamOwnerIdentity(activity.getId());
    if (streamOwner == null || !streamOwner.isSpace()) {
      throw new IllegalArgumentException("report.targetNotInSpaceFeed");
    }
    if (StringUtils.isBlank(reason) || !DEFAULT_REASONS.contains(reason)) {
      throw new IllegalArgumentException("report.invalidReason");
    }

    long reporterId = Long.parseLong(reporterIdentity.getId());
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE_NAME, String.valueOf(reporterId), reporterId);
    // Reports always anchor on the activity/comment itself, never on the
    // redirected content object a content-backed activity (e.g. news) exposes
    // through getMetadataObject(): the duplicate guard below and the stale
    // sweep both query this anchor. AbstractMetadataItemListener exempts
    // report items from its synchronous move-to-target-object so the anchor
    // survives the metadataItem.created/modified listener chain
    MetadataObject metadataObject = buildActivityMetadataObject(activity);
    List<MetadataItem> reporterItems = metadataService.getMetadataItemsByMetadataAndObject(metadataKey, metadataObject);
    MetadataItem staleItem = null;
    for (MetadataItem item : reporterItems) {
      if (isStale(item)) {
        staleItem = item;
      } else {
        throw new ObjectAlreadyExistsException(item, "report.alreadyReported");
      }
    }
    Map<String, String> properties = new HashMap<>();
    properties.put(REASON_PROPERTY, reason);
    properties.put(STATUS_PROPERTY, STATUS_ACTIVE);
    if (staleItem == null) {
      metadataService.createMetadataItem(metadataObject, metadataKey, properties, reporterId);
    } else {
      staleItem.setProperties(properties);
      metadataService.updateMetadataItem(staleItem, reporterId);
    }

    ActivityReport report = new ActivityReport(activity.getId(),
                                               metadataObject.getType(),
                                               metadataObject.getId(),
                                               metadataObject.getParentId(),
                                               reporterId,
                                               reason,
                                               Long.parseLong(streamOwner.getId()));
    broadcastReported(report);
    return report;
  }

  @Override
  public void markReportsStale(ExoSocialActivity activity) {
    if (activity == null) {
      return;
    }
    MetadataObject metadataObject = buildActivityMetadataObject(activity);
    List<MetadataItem> reportItems = metadataService.getMetadataItemsByMetadataTypeAndObject(METADATA_TYPE_NAME, metadataObject);
    for (MetadataItem item : reportItems) {
      if (!isStale(item)) {
        Map<String, String> properties = item.getProperties() == null ? new HashMap<>() : new HashMap<>(item.getProperties());
        properties.put(STATUS_PROPERTY, STATUS_STALE);
        item.setProperties(properties);
        metadataService.updateMetadataItem(item, item.getCreatorId());
      }
    }
  }

  private MetadataObject buildActivityMetadataObject(ExoSocialActivity activity) {
    long spaceId = StringUtils.isBlank(activity.getSpaceId()) ? 0 : Long.parseLong(activity.getSpaceId());
    return new MetadataObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                              activity.getId(),
                              activity.getParentId(),
                              spaceId);
  }

  private boolean isStale(MetadataItem item) {
    return item.getProperties() != null && STATUS_STALE.equals(item.getProperties().get(STATUS_PROPERTY));
  }

  private void broadcastReported(ActivityReport report) {
    try {
      listenerService.broadcast(EVENT_ACTIVITY_REPORTED, report, report.getReporterIdentityId());
    } catch (Exception e) {
      LOG.warn("Error broadcasting event {} for activity {}", EVENT_ACTIVITY_REPORTED, report.getActivityId(), e);
    }
  }

}
