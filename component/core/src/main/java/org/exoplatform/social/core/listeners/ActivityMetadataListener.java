/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.listeners;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataObject;

/**
 * A listener to handle Metadata lifecycle switch Activity lifecycle
 */
public class ActivityMetadataListener extends ActivityListenerPlugin {

  private MetadataService metadataService;

  private ActivityManager activityManager;

  public ActivityMetadataListener(MetadataService metadataService, ActivityManager activityManager) {
    this.metadataService = metadataService;
    this.activityManager = activityManager;
  }

  @Override
  public void deleteActivity(ActivityLifeCycleEvent event) {
    // Cleanup all related Metadatas of the activity once deleted
    String activityId = event.getActivityId();
    metadataService.deleteMetadataItemsByObject(new MetadataObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                                                   activityId));
    metadataService.deleteMetadataItemsByParentObject(new MetadataObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                                                         null,
                                                                         activityId));
  }

  @Override
  public void deleteComment(ActivityLifeCycleEvent event) {
    deleteActivity(event);
  }

  @Override
  public void shareActivity(ActivityLifeCycleEvent event) {
    ExoSocialActivity targetActivity = event.getActivity();
    String targetActivityId = targetActivity.getId();
    String originalSharedActivityId = targetActivity.getTemplateParams()
                                                    .get(ActivityManager.SHARED_ACTIVITY_ID_PARAM);
    String posterId = StringUtils.isBlank(targetActivity.getUserId()) ? targetActivity.getUserId() : targetActivity.getPosterId();
    Identity targetStreamOwnerIdentity = this.activityManager.getActivityStreamOwnerIdentity(targetActivity.getId());
    metadataService.shareMetadataItemsByObject(new MetadataObject(ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE,
                                                                  originalSharedActivityId),
                                               targetActivityId,
                                               Long.parseLong(targetStreamOwnerIdentity.getId()),
                                               Long.parseLong(posterId));
  }

}
