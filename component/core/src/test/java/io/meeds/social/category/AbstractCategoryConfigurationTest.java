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
package io.meeds.social.category;

import java.util.Collections;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import org.exoplatform.social.core.identity.model.Identity;

import io.meeds.social.AbstractSpringConfigurationTest;
import io.meeds.social.category.model.Category;
import io.meeds.social.category.plugin.CategoryTranslationPlugin;
import io.meeds.social.category.service.CategoryLinkService;
import io.meeds.social.category.service.CategoryService;
import io.meeds.social.category.service.CategoryServiceImpl;
import io.meeds.social.translation.service.TranslationService;

import lombok.SneakyThrows;

public abstract class AbstractCategoryConfigurationTest extends AbstractSpringConfigurationTest {

  public static final String    MODULE_NAME               = "io.meeds.social.category";

  public static final String    ROOT_USER                 = "root";

  public static final String    JOHN_USER                 = "john";

  public static final String    MARY_USER                 = "mary";

  public static final String    PLATFORM_USERS_PERMISSION = "*:/platform/users";

  @Autowired
  protected CategoryService     categoryService;

  @Autowired
  protected CategoryLinkService categoryLinkService;

  @Autowired
  protected TranslationService  translationService;

  protected long                usersGroupIdentityId;

  @Before
  public void beforeEach() {
    setUp();
    begin();
    usersGroupIdentityId = Long.parseLong(identityManager.getOrCreateGroupIdentity("/platform/users").getId());
  }

  @After
  @SneakyThrows
  public void afterEach() {
    restartTransaction();
    Category rootCategory = categoryService.getRootCategory(getAdminGroupIdentityId());
    categoryService.deleteCategory(rootCategory.getId(), ROOT_USER);
    end();
    tearDown();
  }

  protected Category buildCategoryTree() {
    long ownerIdentityId = getAdminGroupIdentityId();
    Category rootCategory = categoryService.getRootCategory(ownerIdentityId);
    createTree(ownerIdentityId, rootCategory.getId(), 0, 5);
    return rootCategory;
  }

  @SneakyThrows
  private void createTree(long ownerId, long parentId, long depth, long depthLimit) {
    if (depth < depthLimit) {
      for (int i = 0; i < 2; i++) {
        Category category = new Category();
        category.setOwnerId(ownerId);
        category.setParentId(parentId);
        category.setIcon("test-icon");
        category = categoryService.createCategory(category, ROOT_USER);
        translationService.saveTranslationLabels(CategoryTranslationPlugin.OBJECT_TYPE,
                                                 category.getId(),
                                                 CategoryTranslationPlugin.NAME_FIELD,
                                                 Collections.singletonMap(Locale.ENGLISH,
                                                                          "category-" + parentId + "-" + category.getId()),
                                                 ROOT_USER,
                                                 false);
        createTree(ownerId, category.getId(), depth + 1, depthLimit);
      }
    }
  }

  protected long getAdminGroupIdentityId() {
    Identity adminGroupIdentity = identityManager.getOrCreateGroupIdentity(CategoryServiceImpl.ADMINISTRATORS_GROUP);
    return Long.parseLong(adminGroupIdentity.getId());
  }

  protected long getUsersGroupIdentityId() {
    Identity adminGroupIdentity = identityManager.getOrCreateGroupIdentity("/platform/users");
    return Long.parseLong(adminGroupIdentity.getId());
  }

}
