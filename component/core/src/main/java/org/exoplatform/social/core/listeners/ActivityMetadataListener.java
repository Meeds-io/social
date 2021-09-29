package org.exoplatform.social.core.listeners;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
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
    String activityId = event.getActivityId();
    metadataService.deleteMetadataItemsByObject(MetadataActivityProcessor.ACTIVITY_METADATA_OBJECT_TYPE, activityId);
    metadataService.deleteMetadataItemsByParentObject(MetadataActivityProcessor.COMMENT_METADATA_OBJECT_TYPE, activityId);
  }

  @Override
  public void shareActivity(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getActivity();
    String targetActivityId = activity.getId();
    String sharedActivityId = activity.getTemplateParams().get(ActivityManager.SHARED_ACTIVITY_ID_PARAM);
    String posterId = StringUtils.isBlank(activity.getUserId()) ? activity.getUserId() : activity.getPosterId();
    String streamOwnerId = activity.getActivityStream().getId();
    metadataService.shareMetadataItemsByObject(MetadataActivityProcessor.ACTIVITY_METADATA_OBJECT_TYPE,
                                               sharedActivityId,
                                               targetActivityId,
                                               Long.parseLong(streamOwnerId),
                                               Long.parseLong(posterId));
  }

}
