package org.exoplatform.social.core.listeners;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;
import org.exoplatform.social.metadata.model.MetadataItem;

public class MetadataItemListener extends Listener<Long, MetadataItem> {

  private CachedActivityStorage activityStorage;

  public MetadataItemListener(ActivityStorage activityStorage) {
    if (activityStorage instanceof CachedActivityStorage) {
      this.activityStorage = (CachedActivityStorage) activityStorage;
    }
  }

  @Override
  public void onEvent(Event<Long, MetadataItem> event) throws Exception {
    if (StringUtils.equals(event.getData().getObjectType(), "activity") && this.activityStorage != null) {
      this.activityStorage.clearActivityCached(event.getData().getObjectId());
    }
  }

}
