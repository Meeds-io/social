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

import java.util.Collections;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.exoplatform.social.core.space.SpaceFilter;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.service.CategoryService;
import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.model.SidebarItem;

import lombok.SneakyThrows;

@Component
@Order(50)
public class SpaceCategorySidebarPlugin extends AbstractSpaceSidebarPlugin {

  public static final String SPACE_CATEGORY_ID_PROP_NAME = "spaceCategoryId";

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
    Category category = categoryService.getCategory(categoryId, username, locale);
    if (category != null) {
      item.setName(category.getName());
      item.setIcon(category.getIcon());
      item.setItems(getSpaces(item, username));
    }
    return item;
  }

  @Override
  protected void buildSpaceFilter(SidebarItem item, SpaceFilter spaceFilter) {
    String categoryId = item.getProperties().get(SPACE_CATEGORY_ID_PROP_NAME);
    spaceFilter.setCategoryIds(Collections.singletonList(Long.parseLong(categoryId)));
  }

  private long getCategoryId(SidebarItem item) {
    String spaceCategoryIdProperty = item.getProperties().get(SPACE_CATEGORY_ID_PROP_NAME);
    return Long.parseLong(spaceCategoryIdProperty);
  }

}
