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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.jpa.search.ActivityIndexingServiceConnector;
import org.exoplatform.social.core.jpa.search.SpaceIndexingServiceConnector;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedSpaceStorage;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;

/**
 * Metadata Item listeners that will be triggered once a metadata is
 * added/delete/updated/shared
 * 
 * @param <S> {@link Listener} source object
 * @param <D> {@link Listener} data object
 */
public abstract class AbstractMetadataItemListener<S, D> extends Listener<S, D> {

  private static final Log      LOG = ExoLogger.getLogger(AbstractMetadataItemListener.class);

  private CachedActivityStorage cachedActivityStorage;

  private CachedSpaceStorage    cachedSpaceStorage;

  private MetadataService       metadataService;

  private IndexingService       indexingService;

  protected AbstractMetadataItemListener(MetadataService metadataService,
                                         ActivityStorage activityStorage,
                                         SpaceStorage spaceStorage,
                                         IndexingService indexingService) {
    this.indexingService = indexingService;
    this.metadataService = metadataService;
    if (activityStorage instanceof CachedActivityStorage) {
      this.cachedActivityStorage = (CachedActivityStorage) activityStorage;
    }
    if (spaceStorage instanceof CachedSpaceStorage) {
      this.cachedSpaceStorage = (CachedSpaceStorage) spaceStorage;
    }
  }

  protected void handleMetadataModification(MetadataItem metadataItem) {
    if (metadataItem != null) {
      String objectType = metadataItem.getObjectType();
      String objectId = metadataItem.getObjectId();
      if (isActivityEvent(objectType) && cachedActivityStorage != null) {
        ExoSocialActivity activity = cachedActivityStorage.getActivity(objectId);
        if (activity != null && activity.hasSpecificMetadataObject()) {
          // Copy Metadata definition into specific MetadataObject instead of
          // Activity Object itself
          MetadataObject metadataObject = activity.getMetadataObject();
          moveMetadataItemToTargetObject(metadataItem, metadataObject);
        }
      }
      handleMetadataModification(objectType, metadataItem.getObjectId());
    }
  }

  protected void handleMetadataModification(String objectType, String objectId) {
    if (isActivityEvent(objectType)) {
      // Ensure to re-execute MetadataActivityProcessor to compute & cache
      // metadatas of the activity again
      clearActivityCache(objectId);
      reindexActivity(objectId);
    }  else if (isSpaceEvent(objectType)) {
      clearSpaceCache(objectId);
      reindexSpace(objectId);
    }
  }

  protected boolean isActivityEvent(String objectType) {
    return StringUtils.equals(objectType, ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE);
  }

  protected boolean isSpaceEvent(String objectType) {
    return StringUtils.equals(objectType, Space.DEFAULT_SPACE_METADATA_OBJECT_TYPE);
  }

  private void moveMetadataItemToTargetObject(MetadataItem metadataItem, MetadataObject targetMetadataObject) {
    try {
      metadataService.deleteMetadataItem(metadataItem.getId(), false);
    } catch (Exception e) {
      LOG.warn("Unable to delete newly created item", e);
    }

    MetadataKey metadataKey = metadataItem.getMetadata().key();
    Map<String, String> properties = metadataItem.getProperties();

    try {
      long creatorId = metadataItem.getCreatorId();

      metadataService.createMetadataItem(targetMetadataObject,
                                         metadataKey,
                                         properties,
                                         creatorId);
    } catch (Exception e) {
      LOG.warn("Unable to move Metadata item from {} to {}", metadataItem.getObject(), targetMetadataObject, e);
    }
  }

  private void reindexActivity(String activityId) {
    indexingService.reindex(ActivityIndexingServiceConnector.TYPE, activityId);
  }

  private void clearActivityCache(String activityId) {
    if (cachedActivityStorage != null) {
      cachedActivityStorage.clearActivityCached(activityId);
    }
  }

  private void reindexSpace(String spaceId) {
    indexingService.reindex(SpaceIndexingServiceConnector.TYPE, spaceId);
  }

  private void clearSpaceCache(String spaceId) {
    if (cachedSpaceStorage != null) {
      cachedSpaceStorage.clearSpaceCached(spaceId);
    }
  }

}
