package org.exoplatform.social.core.listeners;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.metadata.model.MetadataObject;

public class MetadataItemShared extends AbstractMetadataItemListener<MetadataObject, String> {

  public MetadataItemShared(ActivityStorage activityStorage, IndexingService indexingService) {
    super(activityStorage, indexingService);
  }

  @Override
  public void onEvent(Event<MetadataObject, String> event) throws Exception {
    MetadataObject sourceObject = event.getSource();
    String targetObjectId = event.getData();
    handleMetadataModification(sourceObject.getType(), targetObjectId);
  }
}
