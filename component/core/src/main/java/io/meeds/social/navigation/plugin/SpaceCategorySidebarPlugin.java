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
package io.meeds.social.navigation.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.exoplatform.social.core.space.SpaceFilter;

import io.meeds.portal.navigation.constant.SidebarItemType;
import io.meeds.portal.navigation.model.SidebarItem;
import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryWithName;
import io.meeds.social.category.service.CategoryService;
import io.meeds.social.util.JsonUtils;

import lombok.SneakyThrows;

@Component
@Order(50)
public class SpaceCategorySidebarPlugin extends AbstractSpaceSidebarPlugin {

  public static final String SPACE_CATEGORY_ID_PROP_NAME  = "spaceCategoryId";

  public static final String SPACE_CATEGORY_IDS_PROP_NAME = "spaceCategoryIds";

  @Autowired
  private CategoryService    categoryService;

  @Override
  public SidebarItemType getType() {
    return SidebarItemType.SPACE_CATEGORY;
  }

  @Override
  public boolean itemExists(SidebarItem item, String username) {
    if (item == null || item.getProperties() == null) {
      return false;
    }
    long categoryId = getCategoryId(item);
    Category category = categoryService.getCategory(categoryId);
    return category != null && categoryService.canAccess(category, username);
  }

  @Override
  @SneakyThrows
  public SidebarItem resolveProperties(SidebarItem item, String username, Locale locale) {
    long categoryId = getCategoryId(item);
    CategoryWithName category = categoryService.getCategory(categoryId, username, locale);
    if (category != null) {
      item.setName(category.getName());
      item.setIcon(category.getIcon());
      item.setItems(getSpaces(item, username));
    }
    return item;
  }

  @Override
  protected void buildSpaceFilter(String username, SidebarItem item, SpaceFilter spaceFilter) {
    long categoryId = getCategoryId(item);
    List<Long> subCategoryIds = categoryService.getSubcategoryIds(username, categoryId, 0, -1, -1);
    List<Long> categoryIds = new ArrayList<>();
    categoryIds.add(categoryId);
    if (CollectionUtils.isNotEmpty(subCategoryIds)) {
      categoryIds.addAll(subCategoryIds);
    }
    item.setProperties(new HashMap<>(item.getProperties()));
    item.getProperties().put(SPACE_CATEGORY_IDS_PROP_NAME, JsonUtils.toJsonString(categoryIds));
    spaceFilter.setCategoryIds(categoryIds);
  }

  private long getCategoryId(SidebarItem item) {
    String spaceCategoryIdProperty = item.getProperties().get(SPACE_CATEGORY_ID_PROP_NAME);
    return Long.parseLong(spaceCategoryIdProperty);
  }

}
