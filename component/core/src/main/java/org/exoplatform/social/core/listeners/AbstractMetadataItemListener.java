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
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.core.jpa.search.ActivityIndexingServiceConnector;
import org.exoplatform.social.core.processor.MetadataActivityProcessor;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;

/**
 * Metadata Item listeners that will be triggered once a metadata is
 * added/delete/updated/shared
 * 
 * @param <S> {@link Listener} source object
 * @param <D> {@link Listener} data object
 */
public abstract class AbstractMetadataItemListener<S, D> extends Listener<S, D> {

  private CachedActivityStorage cachedActivityStorage;

  private IndexingService       indexingService;

  protected AbstractMetadataItemListener(ActivityStorage activityStorage, IndexingService indexingService) {
    this.indexingService = indexingService;
    if (activityStorage instanceof CachedActivityStorage) {
      this.cachedActivityStorage = (CachedActivityStorage) activityStorage;
    }
  }

  protected void handleMetadataModification(String objectType, String objectId) {
    if (isActivityEvent(objectType)) {
      // Ensure to re-execute MetadataActivityProcessor to compute & cache
      // metadatas of the activity again
      clearCache(objectId);
      reindexActivity(objectId);
    }
  }

  private boolean isActivityEvent(String objectType) {
    return StringUtils.equals(objectType, MetadataActivityProcessor.ACTIVITY_METADATA_OBJECT_TYPE);
  }

  private void reindexActivity(String activityId) {
    indexingService.reindex(ActivityIndexingServiceConnector.TYPE, activityId);
  }

  private void clearCache(String activityId) {
    if (cachedActivityStorage != null) {
      cachedActivityStorage.clearActivityCached(activityId);
    }
  }

}
