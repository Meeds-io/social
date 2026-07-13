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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.category.AbstractCategoryConfigurationTest;
import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryFilter;
import io.meeds.social.category.model.CategoryObject;
import io.meeds.social.category.model.CategoryTree;
import io.meeds.social.space.plugin.SpaceCategoryPlugin;

import lombok.SneakyThrows;

public class CategoryLinkServiceTest extends AbstractCategoryConfigurationTest {

  @Test
  @SneakyThrows
  public void testIsLinked() {
    assertFalse(categoryLinkService.isLinked(12254689l, new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE, "5566", 0l)));

    Space space = new Space();
    space.setRegistration(Space.OPEN);
    space.setVisibility(Space.PUBLIC);
    space = spaceService.createSpace(space, ROOT_USER);

    Category rootCategory = categoryService.getRootCategory(getAdminGroupIdentityId());
    CategoryObject object = new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE, space.getId(), space.getSpaceId());
    categoryLinkService.link(rootCategory.getId(), object);

    assertTrue(categoryLinkService.isLinked(rootCategory.getId(), object));
  }

  @Test
  @SneakyThrows
  public void testLink() {
    assertThrows(ObjectNotFoundException.class, () -> categoryLinkService.link(12254689l, new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE, "5566", 0l), MARY_USER));

    Category rootCategory = categoryService.getRootCategory(getAdminGroupIdentityId());
    assertThrows(IllegalAccessException.class, () -> categoryLinkService.link(rootCategory.getId(), new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE, "5566", 0l), MARY_USER));

    Space space = new Space();
    space.setRegistration(Space.OPEN);
    space.setVisibility(Space.PUBLIC);
    space = spaceService.createSpace(space, ROOT_USER);
    CategoryObject object = new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE, space.getId(), space.getSpaceId());

    assertThrows(IllegalAccessException.class, () -> categoryLinkService.link(rootCategory.getId(), object, MARY_USER));

    categoryLinkService.link(rootCategory.getId(), object, ROOT_USER);
    assertThrows(ObjectAlreadyExistsException.class, () -> categoryLinkService.link(rootCategory.getId(), object, ROOT_USER));

    buildCategoryTree();
    CategoryFilter filter = new CategoryFilter(null, 0, 0, 1, 0, 1, false, false);
    CategoryTree categoryTree = categoryService.getCategoryTree(filter, MARY_USER, Locale.FRENCH);
    CategoryTree category = categoryTree.getCategories().getFirst();
    category.setLinkPermissions(Collections.singletonList(PLATFORM_USERS_PERMISSION));
    categoryService.updateCategory(category, JOHN_USER);

    assertFalse(categoryLinkService.isLinked(category.getId(), object));
    assertThrows(IllegalAccessException.class, () -> categoryLinkService.link(rootCategory.getId(), object, MARY_USER));
    assertFalse(categoryLinkService.isLinked(category.getId(), object));

    spaceService.addMember(space, MARY_USER);
    spaceService.setManager(space, MARY_USER, true);
    categoryLinkService.link(category.getId(), object, MARY_USER);
    assertTrue(categoryLinkService.isLinked(category.getId(), object));
  }

  @Test
  @SneakyThrows
  public void testUnLink() {
    assertThrows(ObjectNotFoundException.class, () -> categoryLinkService.unlink(12254689l, new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE, "5566", 0l), MARY_USER));

    Category rootCategory = categoryService.getRootCategory(getAdminGroupIdentityId());
    assertThrows(IllegalAccessException.class, () -> categoryLinkService.unlink(rootCategory.getId(), new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE, "5566", 0l), MARY_USER));

    Space space = new Space();
    space.setRegistration(Space.OPEN);
    space.setVisibility(Space.PUBLIC);
    space = spaceService.createSpace(space, ROOT_USER);
    CategoryObject object = new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE, space.getId(), space.getSpaceId());
    categoryLinkService.link(rootCategory.getId(), object, ROOT_USER);

    assertThrows(IllegalAccessException.class, () -> categoryLinkService.unlink(rootCategory.getId(), object, MARY_USER));

    assertTrue(categoryLinkService.isLinked(rootCategory.getId(), object));
    categoryLinkService.unlink(rootCategory.getId(), object, ROOT_USER);
    assertFalse(categoryLinkService.isLinked(rootCategory.getId(), object));

    categoryLinkService.link(rootCategory.getId(), object, ROOT_USER);
    rootCategory.setLinkPermissions(Collections.singletonList(PLATFORM_USERS_PERMISSION));
    categoryService.updateCategory(rootCategory, JOHN_USER);

    assertThrows(IllegalAccessException.class, () -> categoryLinkService.unlink(rootCategory.getId(), object, MARY_USER));
    assertTrue(categoryLinkService.isLinked(rootCategory.getId(), object));

    spaceService.addMember(space, MARY_USER);
    spaceService.setManager(space, MARY_USER, true);
    categoryLinkService.unlink(rootCategory.getId(), object, MARY_USER);
    assertFalse(categoryLinkService.isLinked(rootCategory.getId(), object));
  }

  @Test
  @SneakyThrows
  public void testGetLinkedObjects() {
    Category rootCategory = categoryService.getRootCategory(getAdminGroupIdentityId());

    Space space1 = new Space();
    space1.setRegistration(Space.OPEN);
    space1.setVisibility(Space.PUBLIC);
    space1 = spaceService.createSpace(space1, ROOT_USER);
    CategoryObject object1 = new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE, space1.getId(), space1.getSpaceId());

    Space space2 = new Space();
    space2.setRegistration(Space.OPEN);
    space2.setVisibility(Space.PUBLIC);
    space2 = spaceService.createSpace(space2, ROOT_USER);
    CategoryObject object2 = new CategoryObject(SpaceCategoryPlugin.OBJECT_TYPE, space2.getId(), space2.getSpaceId());

    List<CategoryObject> linkedObjects = categoryLinkService.getLinkedObjects(rootCategory.getId(),
                                                                              Collections.singletonList(SpaceCategoryPlugin.OBJECT_TYPE),
                                                                              0,
                                                                              10);
    assertTrue(linkedObjects.isEmpty());

    categoryLinkService.link(rootCategory.getId(), object1, ROOT_USER);
    categoryLinkService.link(rootCategory.getId(), object2, ROOT_USER);

    linkedObjects = categoryLinkService.getLinkedObjects(rootCategory.getId(),
                                                         Collections.singletonList(SpaceCategoryPlugin.OBJECT_TYPE),
                                                         0,
                                                         10);
    assertEquals(2, linkedObjects.size());

    linkedObjects = categoryLinkService.getLinkedObjects(rootCategory.getId(),
                                                         Collections.singletonList("notMatchingType"),
                                                         0,
                                                         10);
    assertTrue(linkedObjects.isEmpty());
  }

}
