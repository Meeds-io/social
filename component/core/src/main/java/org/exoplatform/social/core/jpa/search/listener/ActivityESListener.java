package org.exoplatform.social.core.jpa.search.listener;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListener;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.jpa.search.ActivityIndexingServiceConnector;

public class ActivityESListener implements ActivityListener {
  private static final Log LOG = ExoLogger.getExoLogger(ActivityESListener.class);

  @Override
  public void saveActivity(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getActivity();
    if (activity.isHidden()) {
      unindexActivity(event.getActivity(), "hide activity");
    } else {
      reindexActivity(activity, "save activity");
    }
  }

  @Override
  public void updateActivity(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getActivity();
    if (activity.isHidden()) {
      unindexActivity(event.getActivity(), "hide activity");
    } else {
      reindexActivity(activity, "update activity");
    }
  }

  @Override
  public void saveComment(ActivityLifeCycleEvent event) {
    reindexActivity(event.getActivity(), "save comment");
  }

  @Override
  public void updateComment(ActivityLifeCycleEvent event) {
    reindexActivity(event.getActivity(), "update comment");
  }

  @Override
  public void deleteActivity(ActivityLifeCycleEvent event) {
    unindexActivity(event.getActivity(), "delete comment");
  }

  @Override
  public void deleteComment(ActivityLifeCycleEvent event) {
    unindexActivity(event.getActivity(), "delete comment");
  }

  private void reindexActivity(ExoSocialActivity activity, String cause) {
    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    LOG.debug("Notifying indexing service for activity with id={}. Cause: {}", activity.getId(), cause);
    indexingService.reindex(ActivityIndexingServiceConnector.TYPE, activity.getId());
  }

  private void unindexActivity(ExoSocialActivity activity, String cause) {
    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    LOG.debug("Notifying indexing service for activity with id={}. Cause: {}", activity.getId(), cause);
    indexingService.unindex(ActivityIndexingServiceConnector.TYPE, activity.getId());
  }

}
