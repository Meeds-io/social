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

import static io.meeds.social.navigation.plugin.SpaceCategorySidebarPlugin.SPACE_CATEGORY_ID_PROP_NAME;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.exoplatform.social.core.space.model.Space;

import io.meeds.social.category.AbstractCategoryConfigurationTest;
import io.meeds.social.category.model.CategoryFilter;
import io.meeds.social.category.model.CategoryTree;
import io.meeds.social.navigation.constant.SidebarItemType;
import io.meeds.social.navigation.model.SidebarItem;

import lombok.SneakyThrows;

public class SpaceCategorySidebarPluginTest extends AbstractCategoryConfigurationTest {

  @Autowired
  protected SpaceCategorySidebarPlugin spaceCategorySidebarPlugin;

  @Test
  public void testGetType() {
    assertEquals(SidebarItemType.SPACE_CATEGORY, spaceCategorySidebarPlugin.getType());
  }

  @Test
  public void testGetDefaultItems() {
    List<SidebarItem> defaultItems = spaceCategorySidebarPlugin.getDefaultItems();
    assertNotNull(defaultItems);
    assertTrue(defaultItems.isEmpty());
  }

  @Test
  @SneakyThrows
  public void testItemExists() {
    assertFalse(spaceCategorySidebarPlugin.itemExists(null, null));
    assertFalse(spaceCategorySidebarPlugin.itemExists(new SidebarItem(), null));
    assertFalse(spaceCategorySidebarPlugin.itemExists(new SidebarItem(), ROOT_USER));

    SidebarItem item = new SidebarItem(SidebarItemType.SPACE_CATEGORY);
    item.setProperties(Collections.singletonMap(SPACE_CATEGORY_ID_PROP_NAME, "12"));
    assertFalse(spaceCategorySidebarPlugin.itemExists(item, ROOT_USER));

    buildCategoryTree();
    CategoryFilter filter = new CategoryFilter(0, 0, 5, 0, 2, false, false);
    CategoryTree categoryTree = categoryService.getCategoryTree(filter, JOHN_USER, Locale.ENGLISH);
    CategoryTree subCategory1 = categoryTree.getCategories().get(0);
    item.setProperties(Collections.singletonMap(SPACE_CATEGORY_ID_PROP_NAME, String.valueOf(subCategory1.getId())));
    assertTrue(spaceCategorySidebarPlugin.itemExists(item, ROOT_USER));
    assertTrue(spaceCategorySidebarPlugin.itemExists(item, MARY_USER));

    subCategory1.setAccessPermissionIds(Collections.singletonList(getAdminGroupIdentityId()));
    categoryService.updateCategory(subCategory1, JOHN_USER);
    assertTrue(spaceCategorySidebarPlugin.itemExists(item, ROOT_USER));
    assertFalse(spaceCategorySidebarPlugin.itemExists(item, MARY_USER));
  }

  @Test
  public void testResolveProperties() {
    buildCategoryTree();
    CategoryFilter filter = new CategoryFilter(0, 0, 5, 0, 2, false, false);
    CategoryTree categoryTree = categoryService.getCategoryTree(filter, JOHN_USER, Locale.ENGLISH);
    CategoryTree subCategory1 = categoryTree.getCategories().get(0);
    CategoryTree subCategory2 = categoryTree.getCategories().get(1);

    SidebarItem item = new SidebarItem(SidebarItemType.SPACE_CATEGORY);
    item.setProperties(Collections.singletonMap(SPACE_CATEGORY_ID_PROP_NAME, String.valueOf(subCategory1.getId())));
    spaceCategorySidebarPlugin.resolveProperties(item, ROOT_USER, Locale.ENGLISH);

    assertNotNull(item.getName());
    assertEquals(subCategory1.getName(), item.getName());
    assertNotNull(item.getIcon());
    assertEquals(subCategory1.getIcon(), item.getIcon());
    assertNull(item.getUrl());
    assertNull(item.getTarget());
    assertNull(item.getAvatar());

    assertNotNull(item.getItems());
    assertEquals(0, item.getItems().size());

    for (int i = 0; i < 5; i++) {
      Space space = new Space();
      space.setRegistration(Space.OPEN);
      space.setVisibility(Space.PUBLIC);
      space = spaceService.createSpace(space, ROOT_USER);
      space.setCategoryIds(Collections.singletonList(subCategory1.getId()));
      spaceService.updateSpace(space);
    }

    item = new SidebarItem(SidebarItemType.SPACE_CATEGORY);
    item.setProperties(Collections.singletonMap(SPACE_CATEGORY_ID_PROP_NAME, String.valueOf(subCategory1.getId())));
    spaceCategorySidebarPlugin.resolveProperties(item, ROOT_USER, Locale.ENGLISH);

    assertNotNull(item.getItems());
    assertEquals(AbstractSpaceSidebarPlugin.SPACES_LIMIT_DEFAULT, item.getItems().size());

    item = new SidebarItem(SidebarItemType.SPACE_CATEGORY);
    item.setProperties(Collections.singletonMap(SPACE_CATEGORY_ID_PROP_NAME, String.valueOf(subCategory1.getId())));
    spaceCategorySidebarPlugin.resolveProperties(item, MARY_USER, Locale.ENGLISH);

    assertNotNull(item.getItems());
    assertEquals(0, item.getItems().size());

    item = new SidebarItem(SidebarItemType.SPACE_CATEGORY);
    item.setProperties(Collections.singletonMap(SPACE_CATEGORY_ID_PROP_NAME, String.valueOf(subCategory2.getId())));
    spaceCategorySidebarPlugin.resolveProperties(item, ROOT_USER, Locale.ENGLISH);

    assertNotNull(item.getItems());
    assertEquals(0, item.getItems().size());
  }

}
