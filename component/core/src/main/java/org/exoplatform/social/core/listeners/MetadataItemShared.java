package org.exoplatform.social.core.listeners;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.metadata.model.MetadataObjectKey;

public class MetadataItemShared extends AbstractMetadataItemListener<MetadataObjectKey, String> {

  public MetadataItemShared(ActivityStorage activityStorage, IndexingService indexingService) {
    super(activityStorage, indexingService);
  }

  @Override
  public void onEvent(Event<MetadataObjectKey, String> event) throws Exception {
    MetadataObjectKey sourceObject = event.getSource();
    String targetObjectId = event.getData();
    handleMetadataModification(sourceObject.getType(), targetObjectId);
  }
}
