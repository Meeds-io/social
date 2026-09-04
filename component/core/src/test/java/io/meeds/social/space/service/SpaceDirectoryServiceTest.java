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
package io.meeds.social.space.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.service.LayoutService;

import io.meeds.portal.security.constant.UserRegistrationType;
import io.meeds.portal.security.service.SecuritySettingService;
import io.meeds.social.category.model.Category;
import io.meeds.social.category.service.CategoryService;
import io.meeds.social.space.model.SpaceDirectorySettings;
import io.meeds.social.space.storage.SpaceDirectoryStorage;

@SpringBootTest(classes = {
  SpaceDirectoryService.class,
})
@RunWith(SpringRunner.class)
public class SpaceDirectoryServiceTest {

  private static final String USERS_PERMISSION = "users";

  private static final String FILTER_TYPE_TEMPLATE = "template";

  private static final String FILTER_TYPE_CATEGORY = "category";

  private static final String PAGE_REFERENCE = "pageReference";

  private static final String SETTING_NAME = "settingName";

  @MockitoBean
  private CategoryService        categoryService;

  @MockitoBean
  private LayoutService          layoutService;

  @MockitoBean
  private SpaceDirectoryStorage  spaceDirectoryStorage;

  @MockitoBean
  private SecuritySettingService securitySettingService;

  @Autowired
  private SpaceDirectoryService  spaceDirectoryService;

  @Test
  public void testSaveSpacesDirectorySettings() {
    SpaceDirectorySettings spaceDirectorySettings = new SpaceDirectorySettings();
    spaceDirectoryService.saveSpacesDirectorySettings(SETTING_NAME, spaceDirectorySettings);
    verify(spaceDirectoryStorage).save(SETTING_NAME, spaceDirectorySettings);
  }

  @Test
  public void testRemoveSpacesDirectorySettings() {
    spaceDirectoryService.removeSpacesDirectorySettings(SETTING_NAME);
    verify(spaceDirectoryStorage).remove(SETTING_NAME);
  }

  @Test
  public void testCanAccessSpacesDirectoryWithNoFilterWhenRegistrationOpen() {
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME));
    when(securitySettingService.getRegistrationType()).thenReturn(UserRegistrationType.OPEN);
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME));
  }

  @Test
  public void testCanAccessSpacesDirectoryWithNoFilterWhenRegistrationNotOpen() {
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME));
    SpaceDirectorySettings spaceDirectorySettings = new SpaceDirectorySettings();
    when(spaceDirectoryStorage.get(SETTING_NAME)).thenReturn(spaceDirectorySettings);
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME));
    spaceDirectorySettings.setPageReference(PAGE_REFERENCE);
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME));
    Page page = mock(Page.class);
    when(layoutService.getPage(PAGE_REFERENCE)).thenReturn(page);
    when(page.getAccessPermissions()).thenReturn(new String[] {USERS_PERMISSION});
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME));
    when(page.getAccessPermissions()).thenReturn(new String[] {USERS_PERMISSION, UserACL.EVERYONE});
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME));
  }

  @Test
  public void testCanAccessSpacesDirectoryWithFilterWhenRegistrationOpen() {
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
    when(securitySettingService.getRegistrationType()).thenReturn(UserRegistrationType.OPEN);
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
  }

  @Test
  public void testCanAccessSpacesDirectoryWithFiltersWhenRegistrationNotOpen() {
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
    SpaceDirectorySettings spaceDirectorySettings = new SpaceDirectorySettings();
    when(spaceDirectoryStorage.get(SETTING_NAME)).thenReturn(spaceDirectorySettings);
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
    spaceDirectorySettings.setPageReference(PAGE_REFERENCE);
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
    Page page = mock(Page.class);
    when(layoutService.getPage(PAGE_REFERENCE)).thenReturn(page);
    when(page.getAccessPermissions()).thenReturn(new String[] {USERS_PERMISSION});
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
    when(page.getAccessPermissions()).thenReturn(new String[] {USERS_PERMISSION, UserACL.EVERYONE});
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
    spaceDirectorySettings.setFilterType(FILTER_TYPE_CATEGORY);
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
    spaceDirectorySettings.setFilterType(FILTER_TYPE_TEMPLATE);
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
  }

  @Test
  public void testCanAccessSpacesDirectoryWithTemplateFilters() {
    SpaceDirectorySettings spaceDirectorySettings = new SpaceDirectorySettings();
    when(spaceDirectoryStorage.get(SETTING_NAME)).thenReturn(spaceDirectorySettings);
    spaceDirectorySettings.setPageReference(PAGE_REFERENCE);
    Page page = mock(Page.class);
    when(layoutService.getPage(PAGE_REFERENCE)).thenReturn(page);
    when(page.getAccessPermissions()).thenReturn(new String[] {UserACL.EVERYONE});
    spaceDirectorySettings.setFilterType(FILTER_TYPE_TEMPLATE);
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
    spaceDirectorySettings.setTemplateIds(Collections.singletonList(5l));
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, Collections.singletonList(5l)));
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Collections.singletonList(257895l), Collections.singletonList(5l)));
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, Collections.singletonList(15l)));
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, Arrays.asList(5l , 15l)));
  }

  @Test
  public void testCanAccessSpacesDirectoryWithCategoryFilters() {
    SpaceDirectorySettings spaceDirectorySettings = new SpaceDirectorySettings();
    when(spaceDirectoryStorage.get(SETTING_NAME)).thenReturn(spaceDirectorySettings);
    spaceDirectorySettings.setPageReference(PAGE_REFERENCE);
    Page page = mock(Page.class);
    when(layoutService.getPage(PAGE_REFERENCE)).thenReturn(page);
    when(page.getAccessPermissions()).thenReturn(new String[] {UserACL.EVERYONE});
    spaceDirectorySettings.setFilterType(FILTER_TYPE_CATEGORY);
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
    spaceDirectorySettings.setCategoryIds(Collections.singletonList(5l));
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Collections.singletonList(5l), null));
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Collections.singletonList(5l), Collections.singletonList(257895l)));
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Collections.singletonList(15l), null));
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Arrays.asList(5l , 15l), null));
    Category category = new Category();
    category.setId(5l);
    when(categoryService.getCategory(5l)).thenReturn(category);
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Collections.singletonList(5l), null));
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Collections.singletonList(5l), Collections.singletonList(257895l)));
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Collections.singletonList(15l), null));
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Arrays.asList(5l , 15l), null));
    Category otherCategory = new Category();
    otherCategory.setId(15l);
    when(categoryService.getCategory(15l)).thenReturn(otherCategory);
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Collections.singletonList(5l), null));
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Collections.singletonList(5l), Collections.singletonList(257895l)));
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Collections.singletonList(15l), null));
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Arrays.asList(5l , 15l), null));
    when(categoryService.getAncestorIds(15l)).thenReturn(Arrays.asList(5l, 27l));
    assertFalse(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, null, null));
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Collections.singletonList(5l), null));
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Collections.singletonList(5l), Collections.singletonList(257895l)));
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Collections.singletonList(15l), null));
    assertTrue(spaceDirectoryService.canAccessSpacesDirectory(SETTING_NAME, Arrays.asList(5l , 15l), null));
  }

}
