/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.listeners;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.core.jpa.search.ActivityIndexingServiceConnector;
import org.exoplatform.social.core.processor.MetadataActivityProcessor;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObjectKey;

/**
 * Metadata Item listeners that will be triggered once a metadata is
 * added/delete/updated/shared
 */
public class MetadataItemListener {

  private static CachedActivityStorage activityStorage;

  private static IndexingService       indexingService;

  private MetadataItemListener() {
    // Not instanciable class
  }

  protected static void handleMetadataModification(String objectType, String objectId) {
    if (isActivityEvent(objectType)) {
      // Ensure to re-execute MetadataActivityProcessor to compute & cache
      // metadatas of the activity again

      clearCache(objectId);
      reindexActivity(objectId);
    }
  }

  private static boolean isActivityEvent(String objectType) {
    return StringUtils.equals(objectType, MetadataActivityProcessor.ACTIVITY_METADATA_OBJECT_TYPE);
  }

  private static void reindexActivity(String activityId) {
    getIndexingService().reindex(ActivityIndexingServiceConnector.TYPE, activityId);
  }

  private static void clearCache(String activityId) {
    CachedActivityStorage cachedActivityStorage = getActivityStorage();
    if (cachedActivityStorage != null) {
      cachedActivityStorage.clearActivityCached(activityId);
    }
  }

  private static CachedActivityStorage getActivityStorage() {
    if (activityStorage != null) {
      return activityStorage;
    }
    ActivityStorage service = ExoContainerContext.getService(ActivityStorage.class);
    if (service instanceof CachedActivityStorage) {
      activityStorage = (CachedActivityStorage) service;
    }
    return activityStorage;
  }

  private static IndexingService getIndexingService() {
    if (indexingService != null) {
      return indexingService;
    }
    indexingService = ExoContainerContext.getService(IndexingService.class);
    return indexingService;
  }

  public static class MetadataItemModified extends Listener<Long, MetadataItem> {
    @Override
    public void onEvent(Event<Long, MetadataItem> event) throws Exception {
      // If the modified metadata concerns an 'activity'
      MetadataItem metadataItem = event.getData();
      String objectType = event.getData().getObjectType();
      String objectId = metadataItem.getObjectId();
      handleMetadataModification(objectType, objectId);
    }
  }

  public static class MetadataItemShared extends Listener<MetadataObjectKey, String> {
    @Override
    public void onEvent(Event<MetadataObjectKey, String> event) throws Exception {
      MetadataObjectKey sourceObject = event.getSource();
      String targetObjectId = event.getData();
      handleMetadataModification(sourceObject.getType(), targetObjectId);
    }
  }

}
