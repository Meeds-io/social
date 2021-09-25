package org.exoplatform.social.core.listeners;

import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.processor.MetadataActivityProcessor;
import org.exoplatform.social.metadata.MetadataService;

/**
 * A listener to handle Metadata lifecycle switch Activity lifecycle
 */
public class ActivityMetadataListener extends ActivityListenerPlugin {

  private MetadataService metadataService;

  public ActivityMetadataListener(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @Override
  public void deleteActivity(ActivityLifeCycleEvent event) {
    // Cleanup all related Metadatas of the activity once deleted
    metadataService.deleteMetadataItemsByObject(MetadataActivityProcessor.ACTIVITY_METADATA_OBJECT_TYPE, event.getActivityId());
  }

}
