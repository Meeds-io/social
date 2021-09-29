package org.exoplatform.social.core.listeners;

import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.processor.MetadataActivityProcessor;
import org.exoplatform.social.metadata.tag.TagService;

/**
 * A listener to handle Tag update for Activity and Comment lifecycle
 */
public class ActivityTagMetadataListener extends ActivityListenerPlugin {

  private ActivityManager activityManager;

  private TagService      tagService;

  public ActivityTagMetadataListener(ActivityManager activityManager, TagService tagService) {
    this.activityManager = activityManager;
    this.tagService = tagService;
  }

  @Override
  public void saveActivity(ActivityLifeCycleEvent event) {
    updateActivityTags(event.getActivity());
  }

  @Override
  public void saveComment(ActivityLifeCycleEvent event) {
    updateActivityTags(event.getActivity());
  }

  @Override
  public void updateActivity(ActivityLifeCycleEvent event) {
    updateActivityTags(event.getActivity());
  }

  @Override
  public void updateComment(ActivityLifeCycleEvent event) {
    updateActivityTags(event.getActivity());
  }

  private void updateActivityTags(ExoSocialActivity activity) {
    String objectType;
    if (isComment(activity)) {
      objectType = MetadataActivityProcessor.COMMENT_METADATA_OBJECT_TYPE;
    } else {
      objectType = MetadataActivityProcessor.ACTIVITY_METADATA_OBJECT_TYPE;
    }

    long creatorId = getPosterId(activity);

    Identity audienceIdentity = activityManager.getActivityStreamOwnerIdentity(activity.getId());
    long audienceId = Long.parseLong(audienceIdentity.getId());
    String content = getActivityBody(activity);

    Set<String> tagNames = tagService.detectTags(content);
    tagService.saveTags(objectType,
                        activity.getId(),
                        activity.getParentId(),
                        audienceId,
                        creatorId,
                        tagNames);
  }

  private long getPosterId(ExoSocialActivity activity) {
    String userId = activity.getUserId();
    if (StringUtils.isBlank(userId)) {
      userId = activity.getPosterId();
    }
    return StringUtils.isBlank(userId) ? 0 : Long.parseLong(userId);
  }

  private String getActivityBody(ExoSocialActivity activity) {
    String body = MapUtils.getString(activity.getTemplateParams(), "comment");
    if (StringUtils.isNotBlank(body)) {
      return body;
    } else if (StringUtils.isNotBlank(activity.getTitle())) {
      return activity.getTitle();
    } else {
      return activity.getBody();
    }
  }

  private boolean isComment(ExoSocialActivity activity) {
    return StringUtils.isNotBlank(activity.getParentId());
  }

}
