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
package io.meeds.social.category.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.service.CategoryService;
import io.meeds.social.translation.plugin.TranslationPlugin;
import io.meeds.social.translation.service.TranslationService;

import jakarta.annotation.PostConstruct;

@Component
public class CategoryTranslationPlugin extends TranslationPlugin {

  public static final String OBJECT_TYPE = "category";

  public static final String NAME_FIELD  = "name";

  @Autowired
  private TranslationService translationService;

  @Autowired
  private CategoryService    categoryService;

  @PostConstruct
  public void init() {
    translationService.addPlugin(this);
  }

  @Override
  public String getObjectType() {
    return OBJECT_TYPE;
  }

  @Override
  public boolean hasAccessPermission(long categoryId, String username) throws ObjectNotFoundException {
    return categoryService.canAccess(categoryId, username);
  }

  @Override
  public boolean hasEditPermission(long categoryId, String username) throws ObjectNotFoundException {
    return categoryService.canEdit(categoryId, username);
  }

  @Override
  public long getAudienceId(long categoryId) throws ObjectNotFoundException {
    Category category = categoryService.getCategory(categoryId);
    return category == null ? 0 : category.getOwnerId();
  }

  @Override
  public long getSpaceId(long categoryId) throws ObjectNotFoundException {
    return 0;
  }

}
