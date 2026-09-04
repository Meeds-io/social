/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
import org.exoplatform.social.core.jpa.storage.SpaceStorage;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedSpaceStorage;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataObject;

import io.meeds.social.report.service.ActivityReportService;

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
    if (activityStorage instanceof CachedActivityStorage cachedStorage) {
      this.cachedActivityStorage = cachedStorage;
    }
    if (spaceStorage instanceof CachedSpaceStorage cachedStorage) {
      this.cachedSpaceStorage = cachedStorage;
    }
  }

  protected void handleMetadataModification(MetadataItem metadataItem) { // NOSONAR
    if (metadataItem != null) {
      String objectType = metadataItem.getObjectType();
      String objectId = metadataItem.getObjectId();
      if (cachedActivityStorage != null) {
        if (isActivityEvent(objectType)) {
          ExoSocialActivity activity = cachedActivityStorage.getActivity(objectId);
          if (activity != null && activity.hasSpecificMetadataObject()) {
            if (isReportMetadataItem(metadataItem)) {
              // Report items are exempted from the move below: their duplicate
              // guard and stale sweep query the activity anchor
              // (ActivityReportService), so moving them to the redirected
              // content object would silently disable both. A redirected
              // activity is cached under its redirected metadata object, so
              // the selector-based eviction of
              // handleMetadataModification(objectType, objectId) never matches
              // it: evict it by key here so the derived canReport/hasReported
              // flags are recomputed on the next read
              cachedActivityStorage.clearActivityCached(objectId);
              reindexActivity(objectId);
            } else {
              // Copy Metadata definition into specific MetadataObject instead
              // of Activity Object itself
              MetadataObject metadataObject = activity.getMetadataObject();
              moveMetadataItemToTargetObject(metadataItem, metadataObject);
            }
          }
        } else if (metadataItem.getSpaceId() >= 0 && cachedSpaceStorage != null) {
          Space space = cachedSpaceStorage.getSpaceById(metadataItem.getSpaceId());
          if (space != null) {
            // Activities can have different object type & id, thus
            // systematically Trigger clearing cache when it's about a space
            // metadata
            cachedActivityStorage.clearOwnerStreamCache(space.getPrettyName());
            clearSpaceCache(space.getId());
          }
        }
      }
      handleMetadataModification(objectType, metadataItem.getObjectId());
    }
  }

  protected void handleMetadataDeletion(MetadataItem metadataItem) {
    String objectType = metadataItem.getObjectType();
    String objectId = metadataItem.getObjectId();
    handleMetadataModification(objectType, objectId);
    if (metadataItem.getSpaceId() > 0) {
      clearSpaceCache(String.valueOf(metadataItem.getSpaceId()));
    }
  }

  protected void handleMetadataModification(String objectType, String objectId) {
    if (clearActivityCache(objectType, objectId) && isActivityEvent(objectType)) {
      // Ensure to re-execute MetadataActivityProcessor to compute & cache
      // metadatas of the activity again
      reindexActivity(objectId);
    }
    if (isSpaceEvent(objectType)) {
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

  private boolean isReportMetadataItem(MetadataItem metadataItem) {
    return metadataItem.getMetadata() != null
           && StringUtils.equals(metadataItem.getMetadata().getTypeName(), ActivityReportService.METADATA_TYPE_NAME);
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

  private boolean clearActivityCache(String objectType, String objectId) {
    if (cachedActivityStorage != null) {
      return cachedActivityStorage.clearActivityByMetadataObject(objectType, objectId);
    }
    return false;
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
