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
package io.meeds.social.category.listener;

import static io.meeds.social.category.service.CategoryLinkService.EVENT_CATEGORY_LINK_ADDED;
import static io.meeds.social.category.service.CategoryLinkService.EVENT_CATEGORY_LINK_REMOVED;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerBase;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.activity.plugin.ActivityCategoryPlugin;
import io.meeds.social.category.model.CategoryObject;
import io.meeds.social.space.category.service.SpaceCategoryService;
import io.meeds.social.space.plugin.SpaceCategoryPlugin;

import jakarta.annotation.PostConstruct;

@Component
public class CategoryLinkModifiedListener implements ListenerBase<Long, CategoryObject> {

  @Autowired
  private SpaceService         spaceService;

  @Autowired
  private ActivityManager      activityManager;

  @Autowired
  private SpaceCategoryService spaceCategoryService;

  @Autowired
  private ListenerService      listenerService;

  @PostConstruct
  public void init() {
    listenerService.addListener(EVENT_CATEGORY_LINK_ADDED, this);
    listenerService.addListener(EVENT_CATEGORY_LINK_REMOVED, this);
  }

  @Override
  public void onEvent(Event<Long, CategoryObject> event) throws Exception {
    CategoryObject object = event.getData();
    String type = object.getType();
    switch (type) {
    case SpaceCategoryPlugin.OBJECT_TYPE:
      Space space = spaceService.getSpaceById(Long.parseLong(object.getId()));
      if (space != null) {
        List<Long> categoryIds = spaceCategoryService.getSpaceCategoryIds(space.getSpaceId());
        if (CollectionUtils.size(categoryIds) != CollectionUtils.size(space.getCategoryIds())
            || (CollectionUtils.size(categoryIds) > 0 && !CollectionUtils.isEqualCollection(categoryIds, space.getCategoryIds()))) {
          space.setCategoryIds(categoryIds);
          spaceService.updateSpace(space);
        }
      }
      break;
    case ActivityCategoryPlugin.OBJECT_TYPE:
      String activityId = object.getId();
      ExoSocialActivity activity = activityManager.getActivity(activityId);
      if (activity != null) {
        // Update the cached categoryIds incrementally from the event itself instead of
        // re-querying the storage: the link/unlink that triggered this event may not be
        // visible yet to a fresh query within the same request/transaction.
        Long categoryId = event.getSource();
        List<Long> categoryIds = activity.getCategoryIds() == null ? new ArrayList<>() : new ArrayList<>(activity.getCategoryIds());
        boolean changed;
        if (EVENT_CATEGORY_LINK_ADDED.equals(event.getEventName())) {
          changed = !categoryIds.contains(categoryId) && categoryIds.add(categoryId);
        } else {
          changed = categoryIds.remove(categoryId);
        }
        if (changed) {
          activity.setCategoryIds(categoryIds);
          activityManager.updateActivity(activity);
        }
      }
      break;
    default:
      break;
    }
  }

}
