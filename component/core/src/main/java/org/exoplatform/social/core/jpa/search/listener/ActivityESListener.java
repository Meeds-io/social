/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.social.core.jpa.search.listener;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListener;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.jpa.search.ActivityIndexingServiceConnector;

import io.meeds.activity.space.plugin.ActivityCategoryLifeCycleEvent;

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
  public void updateCategories(ActivityCategoryLifeCycleEvent event) {
    ExoSocialActivity activity = event.getActivity();
    if (activity.isHidden()) {
      unindexActivity(activity, "hide activity");
    } else {
      reindexActivity(activity, "update category");
    }
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
