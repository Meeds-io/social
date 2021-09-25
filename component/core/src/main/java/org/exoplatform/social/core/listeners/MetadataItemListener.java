package org.exoplatform.social.core.listeners;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.search.ActivityIndexingServiceConnector;
import org.exoplatform.social.core.processor.MetadataActivityProcessor;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;
import org.exoplatform.social.metadata.model.MetadataItem;

/**
 * A listener that will be triggered once a metadata is added/delete/updated
 */
public class MetadataItemListener extends Listener<Long, MetadataItem> {

  private static final Log      LOG = ExoLogger.getExoLogger(MetadataItemListener.class);

  private CachedActivityStorage activityStorage;

  private IndexingService       indexingService;

  public MetadataItemListener(IndexingService indexingService, ActivityStorage activityStorage) {
    this.indexingService = indexingService;
    if (activityStorage instanceof CachedActivityStorage) {
      this.activityStorage = (CachedActivityStorage) activityStorage;
    }
  }

  @Override
  public void onEvent(Event<Long, MetadataItem> event) throws Exception {
    // If the modified metadata concerns an 'activity'
    if (isActivityEvent(event)) {
      // Ensure to re-execute MetadataActivityProcessor to compute & cache
      // metadatas of the activity again
      MetadataItem metadataItem = event.getData();
      String activityId = metadataItem.getObjectId();

      clearCache(activityId);
      reindexActivity(event, metadataItem, activityId);
    }
  }

  private boolean isActivityEvent(Event<Long, MetadataItem> event) {
    return StringUtils.equals(event.getData().getObjectType(), MetadataActivityProcessor.ACTIVITY_METADATA_OBJECT_TYPE)
        && this.activityStorage != null;
  }

  private void reindexActivity(Event<Long, MetadataItem> event, MetadataItem metadataItem, String activityId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Notifying indexing service for activity with id={}. Cause: Metadata item '{}' event: {}",
                activityId,
                metadataItem.toString(),
                event.getEventName());
    }
    indexingService.reindex(ActivityIndexingServiceConnector.TYPE, activityId);
  }

  private void clearCache(String activityId) {
    this.activityStorage.clearActivityCached(activityId);
  }

}
