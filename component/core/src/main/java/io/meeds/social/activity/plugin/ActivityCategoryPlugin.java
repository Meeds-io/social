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
package io.meeds.social.activity.plugin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.manager.ActivityManager;

import io.meeds.social.category.model.CategoryObject;
import io.meeds.social.category.plugin.CategoryPlugin;

@Component
public class ActivityCategoryPlugin implements CategoryPlugin {

  public static final String OBJECT_TYPE            = ExoSocialActivityImpl.DEFAULT_ACTIVITY_METADATA_OBJECT_TYPE;

  @Autowired
  private ActivityManager    activityManager;

  @Autowired
  private UserACL            userAcl;

  @Override
  public String getType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean canAccess(String activityId, String username) {
    return userAcl.hasAccessPermission(OBJECT_TYPE, activityId, username);
  }

  @Override
  public boolean canEdit(String activityId, String username) {
    return userAcl.hasPermission(OBJECT_TYPE, activityId, ActivityAclPlugin.MANAGE_PERMISSION_TYPE, username);
  }

  @Override
  public List<Long> getCategoryIds() {
    return getCategoryIds(0l);
  }

  @Override
  public List<Long> getCategoryIds(long spaceId) {
    return activityManager.getActivityCategoryIds(spaceId);
  }

  @Override
  public List<Long> getCategoryIds(long spaceId, String username) {
    return activityManager.getActivityCategoryIds(spaceId, username);
  }

  @Override
  public CategoryObject getObject(CategoryObject categoryObject) {
    ExoSocialActivity activity = activityManager.getActivity(categoryObject.getId());
    return activity == null ? categoryObject : new CategoryObject(activity.getMetadataObject());
  }

}
