/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2021 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.listeners;

import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.processor.MetadataActivityProcessor;
import org.exoplatform.social.metadata.tag.TagService;
import org.exoplatform.social.metadata.tag.model.TagName;
import org.exoplatform.social.metadata.tag.model.TagObject;

/**
 * A listener to handle Tag update for Activity and Comment lifecycle
 */
public class ActivityTagMetadataListener extends ActivityListenerPlugin {

  private ActivityManager  activityManager;

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
    String objectType = MetadataActivityProcessor.ACTIVITY_METADATA_OBJECT_TYPE;

    long creatorId = getPosterId(activity);

    Identity audienceIdentity = activityManager.getActivityStreamOwnerIdentity(activity.getId());
    long audienceId = Long.parseLong(audienceIdentity.getId());
    String content = getActivityBody(activity);

    Set<TagName> tagNames = tagService.detectTagNames(content);
    tagService.saveTags(new TagObject(objectType,
                                      activity.getId(),
                                      activity.getParentId()),
                        tagNames,
                        audienceId,
                        creatorId);
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

}
