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
package io.meeds.social.category.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.exoplatform.container.PortalContainer;

import io.meeds.social.category.model.CategoryEntryItem;
import io.meeds.social.category.model.CategoryObject;
import io.meeds.social.category.plugin.CategoryPlugin;
import io.meeds.social.category.plugin.DefaultCategoryPlugin;

@Service
public class CategoryPluginServiceImpl implements CategoryPluginService {

  @Autowired
  private PortalContainer             container;

  private List<CategoryPlugin>        categoryPlugins = new ArrayList<>();

  private Map<String, CategoryPlugin> categoryPluginsByType = new ConcurrentHashMap<>();

  @Override
  public void addPlugin(CategoryPlugin categoryPlugin) {
    categoryPlugins.add(categoryPlugin);
  }

  @Override
  public CategoryPlugin getCategoryPlugin(String objectType) {
    return categoryPluginsByType.computeIfAbsent(objectType,
                                                 t -> categoryPlugins.stream()
                                                                     .filter(c -> c.getType().equals(t))
                                                                     .findFirst()
                                                                     .orElseGet(() -> new DefaultCategoryPlugin(container,
                                                                                                                objectType)));
  }

  @Override
  public boolean canEdit(String objectType, String objectId, String username) {
    return getCategoryPlugin(objectType).canEdit(objectId, username);
  }

  @Override
  public boolean canAccess(String objectType, String objectId, String username) {
    return getCategoryPlugin(objectType).canAccess(objectId, username);
  }

  @Override
  public CategoryEntryItem getEntryItem(String objectType, String objectId, String username) {
    return getCategoryPlugin(objectType).getEntryItem(objectId, username);
  }

  @Override
  public List<Long> getCategoryIds(String objectType, long spaceId, String username) {
    return getCategoryPlugin(objectType).getCategoryIds(spaceId, username);
  }

  @Override
  public CategoryObject getObject(CategoryObject object) {
    return getCategoryPlugin(object.getType()).getObject(object);
  }

}
