/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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

import static io.meeds.social.category.service.CategoryService.EVENT_SOCIAL_CATEGORY_CREATED;
import static io.meeds.social.category.service.CategoryService.EVENT_SOCIAL_CATEGORY_DELETED;
import static io.meeds.social.category.service.CategoryService.EVENT_SOCIAL_CATEGORY_UPDATED;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerBase;
import org.exoplatform.services.listener.ListenerService;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.service.CategoryService;
import io.meeds.social.category.storage.elasticsearch.CategoryIndexingConnector;

import jakarta.annotation.PostConstruct;

@Component
public class CategoryModifiedIndexingListener implements ListenerBase<Category, Object> {

  @Autowired
  private ListenerService listenerService;

  @Autowired
  private IndexingService indexingService;

  @Autowired
  private CategoryService categoryService;

  @PostConstruct
  public void init() {
    listenerService.addListener(EVENT_SOCIAL_CATEGORY_CREATED, this);
    listenerService.addListener(EVENT_SOCIAL_CATEGORY_UPDATED, this);
    listenerService.addListener(EVENT_SOCIAL_CATEGORY_DELETED, this);
  }

  @Override
  public void onEvent(Event<Category, Object> event) throws Exception {
    switch (event.getEventName()) {
    case EVENT_SOCIAL_CATEGORY_CREATED: {
      indexingService.index(CategoryIndexingConnector.TYPE, String.valueOf(event.getSource().getId()));
      break;
    }
    case EVENT_SOCIAL_CATEGORY_UPDATED: {
      // Ensure to reindex tree to update indexed access permissions
      reindexTree(event.getSource().getId());
      break;
    }
    case EVENT_SOCIAL_CATEGORY_DELETED: {
      indexingService.unindex(CategoryIndexingConnector.TYPE, String.valueOf(event.getSource().getId()));
      break;
    }
    default:
      throw new IllegalArgumentException("Unexpected event name: " + event.getEventName());
    }
  }

  private void reindexTree(long id) {
    indexingService.reindex(CategoryIndexingConnector.TYPE, String.valueOf(id));
    List<Long> subCategoryIds = categoryService.getSubcategoryIds(id, 0, -1);
    if (CollectionUtils.isNotEmpty(subCategoryIds)) {
      subCategoryIds.forEach(this::reindexTree);
    }
  }

}
