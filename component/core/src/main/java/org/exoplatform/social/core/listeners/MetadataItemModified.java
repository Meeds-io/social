package org.exoplatform.social.core.listeners;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.metadata.model.MetadataItem;

public class MetadataItemModified extends AbstractMetadataItemListener<Long, MetadataItem> {

  public MetadataItemModified(ActivityStorage activityStorage, IndexingService indexingService) {
    super(activityStorage, indexingService);
  }

  @Override
  public void onEvent(Event<Long, MetadataItem> event) throws Exception {
    // If the modified metadata concerns an 'activity'
    MetadataItem metadataItem = event.getData();
    String objectType = event.getData().getObjectType();
    String objectId = metadataItem.getObjectId();
    handleMetadataModification(objectType, objectId);
  }
}
