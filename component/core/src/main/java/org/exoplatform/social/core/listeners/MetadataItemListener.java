package org.exoplatform.social.core.listeners;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.core.processor.MetadataActivityProcessor;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;
import org.exoplatform.social.metadata.model.MetadataItem;

/**
 * A listener that will be triggered once a metadata is added/delete/updated
 */
public class MetadataItemListener extends Listener<Long, MetadataItem> {

  private CachedActivityStorage activityStorage;

  public MetadataItemListener(ActivityStorage activityStorage) {
    if (activityStorage instanceof CachedActivityStorage) {
      this.activityStorage = (CachedActivityStorage) activityStorage;
    }
  }

  @Override
  public void onEvent(Event<Long, MetadataItem> event) throws Exception {
    // If the modified metadata concerns an 'activity'
    if (StringUtils.equals(event.getData().getObjectType(), MetadataActivityProcessor.ACTIVITY_METADATA_TYPE)
        && this.activityStorage != null) {
      // Ensure to re-execute MetadataActivityProcessor to compute & cache
      // metadatas of the activity again
      this.activityStorage.clearActivityCached(event.getData().getObjectId());
    }
  }

}
