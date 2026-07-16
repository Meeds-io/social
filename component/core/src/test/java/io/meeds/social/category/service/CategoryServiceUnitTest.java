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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryEntryItem;
import io.meeds.social.category.model.CategoryEntryList;
import io.meeds.social.category.model.CategoryObject;
import io.meeds.social.category.model.CategorySearchFilter;
import io.meeds.social.category.model.CategorySearchResult;
import io.meeds.social.category.storage.CategoryStorage;
import io.meeds.social.translation.service.TranslationService;

@SpringBootTest(classes = {
  CategoryServiceImpl.class,
})
@RunWith(SpringRunner.class)
public class CategoryServiceUnitTest {

  private static final long     CATEGORY_ID             = 7l;

  private static final String   SPACE_PRETTY_NAME       = "prettyName";

  private static final String   SPACE_GROUP_ID_1        = "/spaces/space1";

  private static final String   SPACE_GROUP_ID_2        = "/spaces/space2";

  private static final String   A_GROUP_ID              = "/group1";

  private static final long     ADMIN_GROUP_IDENTITY_ID = 54456l;

  private static final long     SPACE_IDENTITY_ID       = 74456l;

  private static final long     GROUP_IDENTITY_ID       = 34456l;

  private static final boolean  SORT_BY_NAME            = false;

  private static final boolean  LINK_PERMISSION         = true;

  private static final int      LIMIT                   = 30;

  private static final int      OFFSET                  = 10;

  private static final long     PARENT_ID               = 5l;

  private static final long     OWNER_ID                = 2l;

  private static final String   TERM                    = "term";

  private static final String   TEST_USER               = "testuser";

  private static final String   TYPE_A                  = "typeA";

  private static final String   TYPE_B                  = "typeB";

  @MockBean
  private IdentityManager       identityManager;

  @MockBean
  private TranslationService    translationService;

  @MockBean
  private CategoryStorage       categoryStorage;

  @MockBean
  private SpaceService          spaceService;

  @MockBean
  private UserACL               userAcl;

  @MockBean
  private CategoryPluginService categoryPluginService;

  @Autowired
  private CategoryServiceImpl   categoryService;

  @Test
  public void testFindCategories() {
    List<CategorySearchResult> categories = categoryService.findCategories(new CategorySearchFilter(TERM,
                                                                                                    null,
                                                                                                    OWNER_ID,
                                                                                                    PARENT_ID,
                                                                                                    OFFSET,
                                                                                                    LIMIT,
                                                                                                    LINK_PERMISSION,
                                                                                                    SORT_BY_NAME),
                                                                           TEST_USER,
                                                                           Locale.ENGLISH);
    assertNotNull(categories);
    assertEquals(0, categories.size());
    Category parentCategory = mock(Category.class);
    when(parentCategory.getId()).thenReturn(PARENT_ID);
    when(categoryStorage.getCategory(PARENT_ID)).thenReturn(parentCategory);
    Identity adminGroupIdentity = mock(Identity.class);
    when(adminGroupIdentity.getId()).thenReturn(String.valueOf(ADMIN_GROUP_IDENTITY_ID));
    when(identityManager.getOrCreateGroupIdentity(CategoryServiceImpl.ADMINISTRATORS_GROUP)).thenReturn(adminGroupIdentity);
    CategorySearchFilter filter = new CategorySearchFilter(TERM,
                                                           null,
                                                           ADMIN_GROUP_IDENTITY_ID,
                                                           PARENT_ID,
                                                           OFFSET,
                                                           LIMIT,
                                                           LINK_PERMISSION,
                                                           SORT_BY_NAME);
    categories = categoryService.findCategories(filter,
                                                TEST_USER,
                                                Locale.ENGLISH);
    assertNotNull(categories);
    assertEquals(0, categories.size());

    org.exoplatform.services.security.Identity userAclIdentity = mock(org.exoplatform.services.security.Identity.class);
    when(userAcl.getUserIdentity(TEST_USER)).thenReturn(userAclIdentity);
    when(userAclIdentity.getGroups()).thenReturn(new HashSet<>(Arrays.asList(SPACE_GROUP_ID_1, SPACE_GROUP_ID_2, A_GROUP_ID)));

    Space space = mock(Space.class);
    when(space.getPrettyName()).thenReturn(SPACE_PRETTY_NAME);
    when(spaceService.getSpaceByGroupId(SPACE_GROUP_ID_1)).thenReturn(space);

    Identity spaceIdentity = mock(Identity.class);
    when(identityManager.getOrCreateSpaceIdentity(SPACE_PRETTY_NAME)).thenReturn(spaceIdentity);
    when(spaceIdentity.getId()).thenReturn(String.valueOf(SPACE_IDENTITY_ID));

    Identity groupIdentity = mock(Identity.class);
    when(identityManager.getOrCreateGroupIdentity(A_GROUP_ID)).thenReturn(groupIdentity);
    when(groupIdentity.getId()).thenReturn(String.valueOf(GROUP_IDENTITY_ID));

    categories = categoryService.findCategories(filter,
                                                TEST_USER,
                                                Locale.ENGLISH);
    assertNotNull(categories);
    assertEquals(0, categories.size());

    Category category = mock(Category.class);
    when(category.getId()).thenReturn(CATEGORY_ID);
    when(category.getParentId()).thenReturn(PARENT_ID);
    when(categoryStorage.getCategory(CATEGORY_ID)).thenReturn(category);
    when(categoryStorage.findCategories(filter,
                                        Arrays.asList(GROUP_IDENTITY_ID, SPACE_IDENTITY_ID),
                                        Locale.ENGLISH)).thenReturn(Collections.singletonList(category));
    categories = categoryService.findCategories(filter,
                                                TEST_USER,
                                                Locale.ENGLISH);
    assertNotNull(categories);
    assertEquals(1, categories.size());
    assertEquals(Collections.singletonList(PARENT_ID), categories.get(0).getAncestorIds());
  }

  @Test
  public void testGetCategoryEntries() {
    List<String> objectTypes = Arrays.asList(TYPE_A, TYPE_B);
    List<CategoryObject> linkedObjects = Arrays.asList(new CategoryObject(TYPE_A, "1", 0l),
                                                       new CategoryObject(TYPE_B, "1", 0l),
                                                       new CategoryObject(TYPE_A, "2", 0l),
                                                       new CategoryObject(TYPE_B, "3", 0l),
                                                       new CategoryObject(TYPE_A, "4", 0l));
    when(categoryStorage.getLinkedItems(CATEGORY_ID, objectTypes, 0, 6)).thenReturn(linkedObjects);

    when(categoryPluginService.canAccess(TYPE_A, "1", TEST_USER)).thenReturn(true);
    when(categoryPluginService.canAccess(TYPE_B, "1", TEST_USER)).thenReturn(true);
    when(categoryPluginService.canAccess(TYPE_A, "2", TEST_USER)).thenReturn(false);
    when(categoryPluginService.canAccess(TYPE_B, "3", TEST_USER)).thenReturn(true);
    when(categoryPluginService.canAccess(TYPE_A, "4", TEST_USER)).thenReturn(true);

    CategoryEntryItem item1 = mock(CategoryEntryItem.class);
    CategoryEntryItem item4 = mock(CategoryEntryItem.class);
    when(categoryPluginService.getEntryItem(TYPE_A, "1", TEST_USER)).thenReturn(item1);
    when(categoryPluginService.getEntryItem(TYPE_B, "3", TEST_USER)).thenReturn(null);
    when(categoryPluginService.getEntryItem(TYPE_A, "4", TEST_USER)).thenReturn(item4);

    CategoryEntryList result = categoryService.getCategoryEntries(CATEGORY_ID, objectTypes, TEST_USER, 0, 2);
    assertNotNull(result);
    // Duplicate entry (typeB/1) dropped in favor of typeA/1, entry typeA/2 dropped (no access),
    // entry typeB/3 dropped (no resolved preview): only typeA/1 and typeA/4 remain.
    assertEquals(2, result.getItems().size());
    assertTrue(result.getItems().contains(item1));
    assertTrue(result.getItems().contains(item4));
    assertEquals(0, result.getOffset());
    assertEquals(2, result.getLimit());
    assertFalse(result.isHasMore());
    // The page filled up exactly at the last raw row (typeA/4, index 4 of 5): the next page
    // must resume right after it.
    assertEquals(5, result.getNextOffset());
  }

  @Test
  public void testGetCategoryEntriesPaginationAcrossPagesDoesNotSkipOrRepeat() {
    // 5 raw rows share the category link, but "2" is filtered out (e.g. no access) - the
    // exact scenario where naively advancing offset by limit (instead of by the number of raw
    // rows actually consumed) would either skip or repeat entries on the next page.
    List<String> objectTypes = Collections.singletonList(TYPE_A);
    List<CategoryObject> firstBatch = Arrays.asList(new CategoryObject(TYPE_A, "1", 0l),
                                                    new CategoryObject(TYPE_A, "2", 0l),
                                                    new CategoryObject(TYPE_A, "3", 0l),
                                                    new CategoryObject(TYPE_A, "4", 0l),
                                                    new CategoryObject(TYPE_A, "5", 0l));
    when(categoryStorage.getLinkedItems(CATEGORY_ID, objectTypes, 0, 6)).thenReturn(firstBatch);
    when(categoryPluginService.canAccess(TYPE_A, "1", TEST_USER)).thenReturn(true);
    when(categoryPluginService.canAccess(TYPE_A, "2", TEST_USER)).thenReturn(false);
    when(categoryPluginService.canAccess(TYPE_A, "3", TEST_USER)).thenReturn(true);
    when(categoryPluginService.canAccess(TYPE_A, "4", TEST_USER)).thenReturn(true);
    when(categoryPluginService.canAccess(TYPE_A, "5", TEST_USER)).thenReturn(true);
    CategoryEntryItem item1 = mock(CategoryEntryItem.class);
    CategoryEntryItem item3 = mock(CategoryEntryItem.class);
    CategoryEntryItem item4 = mock(CategoryEntryItem.class);
    CategoryEntryItem item5 = mock(CategoryEntryItem.class);
    when(categoryPluginService.getEntryItem(TYPE_A, "1", TEST_USER)).thenReturn(item1);
    when(categoryPluginService.getEntryItem(TYPE_A, "3", TEST_USER)).thenReturn(item3);
    when(categoryPluginService.getEntryItem(TYPE_A, "4", TEST_USER)).thenReturn(item4);
    when(categoryPluginService.getEntryItem(TYPE_A, "5", TEST_USER)).thenReturn(item5);

    CategoryEntryList firstPage = categoryService.getCategoryEntries(CATEGORY_ID, objectTypes, TEST_USER, 0, 2);
    assertEquals(Arrays.asList(item1, item3), firstPage.getItems());
    assertTrue(firstPage.isHasMore());
    // Raw index 1 ("2") was filtered out and consumed without producing an item; the page still
    // filled up at raw index 2 ("3"), so the next page must resume at index 3 - not at offset+limit=2,
    // which would re-fetch "3" and return it twice.
    assertEquals(3, firstPage.getNextOffset());

    List<CategoryObject> secondBatch = Arrays.asList(new CategoryObject(TYPE_A, "4", 0l),
                                                     new CategoryObject(TYPE_A, "5", 0l));
    when(categoryStorage.getLinkedItems(CATEGORY_ID, objectTypes, 3, 6)).thenReturn(secondBatch);

    CategoryEntryList secondPage = categoryService.getCategoryEntries(CATEGORY_ID, objectTypes, TEST_USER, firstPage.getNextOffset(), 2);
    assertEquals(Arrays.asList(item4, item5), secondPage.getItems());
    assertFalse(secondPage.isHasMore());
  }

}
