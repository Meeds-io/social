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

  public static class MetadataItemShared extends Listener<String, String> {
    @Override
    public void onEvent(Event<String, String> event) throws Exception {
      String objectType = event.getData();
      String objectId = event.getSource();
      handleMetadataModification(objectType, objectId);
    }
  }

}
