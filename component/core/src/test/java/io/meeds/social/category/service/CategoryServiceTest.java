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
package io.meeds.social.category.service;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.category.AbstractCategoryConfigurationTest;
import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryFilter;
import io.meeds.social.category.model.CategoryTree;

import lombok.SneakyThrows;

public class CategoryServiceTest extends AbstractCategoryConfigurationTest {

  @Test
  @SneakyThrows
  public void testGetRootCategory() {
    Category rootCategory = categoryService.getRootCategory(getAdminGroupIdentityId());
    assertNotNull(rootCategory);
    assertEquals(getAdminGroupIdentityId(), rootCategory.getOwnerId());

    assertNotNull(categoryService.getRootCategory(getAdminGroupIdentityId()));
    assertEquals(rootCategory.getId(), categoryService.getRootCategory(getAdminGroupIdentityId()).getId());

    Category johnRootCategory = categoryService.getRootCategory(Long.parseLong(identityManager.getOrCreateUserIdentity(JOHN_USER)
                                                                                              .getId()));
    assertNull(johnRootCategory);
  }

  @Test
  @SneakyThrows
  public void testGetCategoryTree() {
    buildCategoryTree();
    long ownerId = 0;
    long parentId = 0;
    long depth = 1;
    long offset = 0;
    long limit = 1;
    CategoryFilter filter = new CategoryFilter(ownerId, parentId, depth, offset, limit, false, false);
    CategoryTree categoryTree = categoryService.getCategoryTree(filter, MARY_USER, Locale.FRENCH);
    assertNotNull(categoryTree);
    assertNotNull(categoryTree.getCategories());
    assertEquals(getAdminGroupIdentityId(), categoryTree.getOwnerId());
    assertEquals(1, categoryTree.getCategories().size());

    limit = 10;
    filter = new CategoryFilter(ownerId, parentId, depth, offset, limit, false, false);
    categoryTree = categoryService.getCategoryTree(filter, MARY_USER, Locale.FRENCH);
    assertNotNull(categoryTree);
    assertNotNull(categoryTree.getCategories());
    assertEquals(2, categoryTree.getCategories().size());

    CategoryTree categoryTree1 = categoryTree.getCategories().get(0);
    assertNotNull(categoryTree1);
    assertNotNull(categoryTree1.getName());
    assertNull(categoryTree1.getCategories());

    CategoryTree categoryTree2 = categoryTree.getCategories().get(1);
    assertNotNull(categoryTree2);
    assertNotNull(categoryTree2.getName());
    assertNull(categoryTree2.getCategories());

    parentId = categoryTree1.getId();
    depth = 10;
    filter = new CategoryFilter(ownerId, parentId, depth, offset, limit, false, false);
    categoryTree = categoryService.getCategoryTree(filter, MARY_USER, Locale.FRENCH);
    assertEquals(categoryTree1.getId(), categoryTree.getId());
    assertEquals(categoryTree1.getName(), categoryTree.getName());
    assertNotNull(categoryTree.getCategories());
    assertEquals(2, categoryTree.getCategories().size());

    assertNotNull(categoryTree.getCategories()
                              .get(0)
                              .getCategories());
    assertNotNull(categoryTree.getCategories()
                              .get(0)
                              .getCategories()
                              .get(0)
                              .getCategories());
    assertNotNull(categoryTree.getCategories()
                              .get(0)
                              .getCategories()
                              .get(0)
                              .getCategories()
                              .get(0)
                              .getCategories());
    assertTrue(CollectionUtils.isEmpty(categoryTree.getCategories()
                                                   .get(0)
                                                   .getCategories()
                                                   .get(0)
                                                   .getCategories()
                                                   .get(0)
                                                   .getCategories()
                                                   .get(0)
                                                   .getCategories()));
  }

  @Test
  @SneakyThrows
  public void testGetAncestorIds() {
    buildCategoryTree();
    long ownerId = 0;
    long parentId = 0;
    long depth = 5;
    long offset = 0;
    long limit = 1;
    CategoryFilter filter = new CategoryFilter(ownerId, parentId, depth, offset, limit, false, false);
    CategoryTree categoryTree = categoryService.getCategoryTree(filter, ROOT_USER, Locale.FRENCH);
    assertNotNull(categoryTree);
    assertNotNull(categoryTree.getCategories());
    assertEquals(getAdminGroupIdentityId(), categoryTree.getOwnerId());
    assertEquals(1, categoryTree.getCategories().size());

    CategoryTree categoryLeaf = categoryTree.getCategories()
                                            .get(0)
                                            .getCategories()
                                            .get(0)
                                            .getCategories()
                                            .get(0)
                                            .getCategories()
                                            .get(0)
                                            .getCategories()
                                            .get(0);
    assertNotNull(categoryLeaf);
    List<Long> ancestorIds = categoryService.getAncestorIds(categoryLeaf.getId());
    assertEquals(5, ancestorIds.size());
    assertEquals(categoryTree.getCategories()
                             .get(0)
                             .getCategories()
                             .get(0)
                             .getCategories()
                             .get(0)
                             .getCategories()
                             .get(0)
                             .getId(),
                 ancestorIds.get(0).longValue());
    assertEquals(categoryTree.getCategories()
                             .get(0)
                             .getCategories()
                             .get(0)
                             .getCategories()
                             .get(0)
                             .getId(),
                 ancestorIds.get(1).longValue());
    assertEquals(categoryTree.getCategories()
                             .get(0)
                             .getCategories()
                             .get(0)
                             .getId(),
                 ancestorIds.get(2).longValue());
    assertEquals(categoryTree.getCategories()
                             .get(0)
                             .getId(),
                 ancestorIds.get(3).longValue());
    assertEquals(categoryTree.getId(),
                 ancestorIds.get(4).longValue());
  }

  @Test
  @SneakyThrows
  public void testCreateCategory() {
    assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(null, JOHN_USER));
    assertThrows(IllegalArgumentException.class, () -> {
      Category category = new Category();
      category.setId(5l);
      categoryService.createCategory(category, JOHN_USER);
    });
    assertThrows(IllegalArgumentException.class, () -> {
      Category category = new Category();
      category.setOwnerId(-2l);
      categoryService.createCategory(category, JOHN_USER);
    });
    assertThrows(ObjectNotFoundException.class, () -> {
      Category category = new Category();
      category.setOwnerId(1l);
      category.setParentId(1544863l);
      categoryService.createCategory(category, JOHN_USER);
    });
    assertThrows(IllegalAccessException.class, () -> {
      Category category = new Category();
      category.setOwnerId(1l);
      categoryService.createCategory(category, MARY_USER);
    });
    long adminGroupIdentityId = getAdminGroupIdentityId();
    Category rootCategory = categoryService.getRootCategory(adminGroupIdentityId);
    assertNotNull(rootCategory);

    assertThrows(ObjectAlreadyExistsException.class, () -> {
      Category category = new Category();
      category.setOwnerId(adminGroupIdentityId);
      categoryService.createCategory(category, MARY_USER);
    });
    Category category = new Category();
    category.setOwnerId(adminGroupIdentityId);
    category.setParentId(rootCategory.getId());

    Category createdCategory = categoryService.createCategory(category, JOHN_USER);
    assertNotNull(createdCategory);
    assertTrue(createdCategory.getId() > 0);
    assertNotEquals(rootCategory.getId(), createdCategory.getId());
  }

  @Test
  @SneakyThrows
  public void testUpdateCategory() {
    Category rootCategory = categoryService.getRootCategory(getAdminGroupIdentityId());
    assertNotNull(rootCategory);

    Category category = new Category();
    category.setOwnerId(getAdminGroupIdentityId());
    category.setParentId(rootCategory.getId());
    category = categoryService.createCategory(category, JOHN_USER);
    long categoryId = category.getId();

    assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(null, JOHN_USER));
    Category categoryToUpdate1 = category;
    assertThrows(IllegalArgumentException.class, () -> {
      categoryToUpdate1.setId(0);
      categoryService.updateCategory(categoryToUpdate1, JOHN_USER);
    });

    category = categoryService.getCategory(categoryId);
    Category categoryToUpdate2 = category;
    assertThrows(IllegalArgumentException.class, () -> {
      categoryToUpdate2.setOwnerId(-2l);
      categoryService.updateCategory(categoryToUpdate2, JOHN_USER);
    });

    category = categoryService.getCategory(categoryId);
    Category categoryToUpdate3 = category;
    assertThrows(ObjectNotFoundException.class, () -> {
      categoryToUpdate3.setParentId(1544863l);
      categoryService.updateCategory(categoryToUpdate3, JOHN_USER);
    });

    category = categoryService.getCategory(categoryId);
    Category categoryToUpdate4 = category;
    assertThrows(ObjectNotFoundException.class, () -> {
      categoryToUpdate4.setId(22333);
      categoryService.updateCategory(categoryToUpdate4, JOHN_USER);
    });

    category = categoryService.getCategory(categoryId);
    Category categoryToUpdate5 = category;
    assertThrows(IllegalAccessException.class, () -> categoryService.updateCategory(categoryToUpdate5, MARY_USER));

    category = categoryService.getCategory(categoryId);
    String icon = "test-icon";
    category.setIcon(icon);
    category = categoryService.updateCategory(category, JOHN_USER);
    assertEquals(icon, category.getIcon());
    category = categoryService.getCategory(categoryId);
    assertEquals(icon, category.getIcon());
  }

  @Test
  @SneakyThrows
  public void testCaneEdit() {
    Category rootCategory = categoryService.getRootCategory(getAdminGroupIdentityId());
    assertNotNull(rootCategory);

    Category category = new Category();
    category.setOwnerId(getAdminGroupIdentityId());
    category.setParentId(rootCategory.getId());
    category = categoryService.createCategory(category, JOHN_USER);

    long categoryId = category.getId();
    assertTrue(categoryService.canEdit(categoryId, JOHN_USER));
    assertFalse(categoryService.canEdit(categoryId, MARY_USER));
    assertFalse(categoryService.canEdit(123456879l, JOHN_USER));
  }

  @Test
  @SneakyThrows
  public void testGetCategory() {
    assertThrows(ObjectNotFoundException.class, () -> categoryService.getCategory(122546l, MARY_USER, Locale.ENGLISH));

    buildCategoryTree();
    long ownerId = getAdminGroupIdentityId();
    long parentId = 0;
    long depth = 10;
    long offset = 0;
    long limit = 10;
    CategoryFilter filter = new CategoryFilter(ownerId, parentId, depth, offset, limit, false, false);
    CategoryTree categoryTree = categoryService.getCategoryTree(filter, MARY_USER, Locale.FRENCH);
    assertEquals(2,
                 categoryTree.getCategories()
                             .get(0)
                             .getCategories()
                             .get(0)
                             .getCategories()
                             .size());

    CategoryTree categoryTree13 = categoryTree.getCategories()
                                              .get(0)
                                              .getCategories()
                                              .get(0)
                                              .getCategories()
                                              .get(0);
    assertNotNull(categoryService.getCategory(categoryTree13.getId(), MARY_USER, Locale.ENGLISH));
    assertNotNull(categoryService.getCategory(categoryTree13.getCategories().get(0).getId(), MARY_USER, Locale.ENGLISH));

    categoryTree13.setAccessPermissionIds(Collections.singletonList(getAdminGroupIdentityId()));
    categoryService.updateCategory(categoryTree13, JOHN_USER);
    assertThrows(IllegalAccessException.class,
                 () -> categoryService.getCategory(categoryTree13.getId(), MARY_USER, Locale.ENGLISH));
    assertThrows(IllegalAccessException.class,
                 () -> categoryService.getCategory(categoryTree13.getCategories().get(0).getId(), MARY_USER, Locale.ENGLISH));

    categoryTree = categoryService.getCategoryTree(filter, MARY_USER, Locale.FRENCH);
    assertNotNull(categoryTree.getCategories()
                              .get(0)
                              .getCategories()
                              .get(0)
                              .getCategories()
                              .get(0));

    filter.setLimit(1);
    categoryTree = categoryService.getCategoryTree(filter, MARY_USER, Locale.FRENCH);
    assertFalse(categoryTree.getCategories()
                            .get(0)
                            .getCategories()
                            .get(0)
                            .getCategories()
                            .isEmpty());
    assertNotNull(categoryTree.getCategories()
                              .get(0)
                              .getCategories()
                              .get(0)
                              .getCategories()
                              .get(0));

    assertNotEquals(categoryTree13.getId(),
                    categoryTree.getCategories()
                                .get(0)
                                .getCategories()
                                .get(0)
                                .getCategories()
                                .get(0)
                                .getId());
  }

}
