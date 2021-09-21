package org.exoplatform.social.core.listeners;

import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.processor.MetadataProcessor;
import org.exoplatform.social.metadata.MetadataService;

public class ActivityMetadataListener extends ActivityListenerPlugin {

  private MetadataService metadataService;

  public ActivityMetadataListener(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @Override
  public void deleteActivity(ActivityLifeCycleEvent event) {
    metadataService.deleteMetadataItemsByObject(MetadataProcessor.ACTIVITY_METADATA_TYPE, event.getActivityId());
  }

}
