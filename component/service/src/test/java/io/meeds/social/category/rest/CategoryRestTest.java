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
package io.meeds.social.category.rest;

import static io.meeds.social.util.JsonUtils.toJsonString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.category.model.Category;
import io.meeds.social.category.model.CategoryFilter;
import io.meeds.social.category.model.CategorySearchFilter;
import io.meeds.social.category.model.CategoryTree;
import io.meeds.social.category.service.CategoryService;
import io.meeds.social.space.service.SpaceDirectoryService;
import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;

import jakarta.servlet.Filter;

@SpringBootTest(classes = { CategoryRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class CategoryRestTest {

  private static final String   CAN_EDIT_CATEGORY_PATH = "/categories/canEdit/5";

  private static final String   CAN_LINK_CATEGORY_PATH = "/categories/canLink/1";

  private static final String   CATEGORIES_PATH        = "/categories";

  private static final String   DELETE_CATEGORY_PATH   = "/categories/1";

  private static final String   SIMPLE_USER            = "simple";

  private static final String   TEST_PASSWORD          = "testPassword";

  @Autowired
  private SecurityFilterChain   filterChain;

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private CategoryService       categoryService;

  @MockitoBean
  private SpaceDirectoryService spaceDirectoryService;

  private MockMvc               mockMvc;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
  }

  @Test
  public void getCategoryTree() throws Exception {
    CategoryTree categoryTree = mock(CategoryTree.class);
    when(categoryService.getCategoryTree(any(), any(), any())).thenReturn(categoryTree);
    ResultActions response = mockMvc.perform(get("/categories?ownerId=1&parentId=2&depth=3&offset=4&limit=5")
                                                                                                             .with(testSimpleUser()));
    response.andExpect(status().isOk());
    verify(categoryService).getCategoryTree(new CategoryFilter(null, 1, 2, 3, 4, 5, false, true, 0), SIMPLE_USER, Locale.ENGLISH);

    response = mockMvc.perform(get("/categories?ownerId=1&parentId=2&depth=3&offset=4&limit=5"));
    response.andExpect(status().isUnauthorized());
  }

  @Test
  public void findCategories() throws Exception {
    when(categoryService.findCategories(any(), any(), any())).thenReturn(Collections.emptyList());
    ResultActions response =
                           mockMvc.perform(get("/categories/search?query=query&ownerId=1&parentId=2&offset=3&limit=4&linkPermission=true&sortByName=true")
                                                                                                                                                          .with(testSimpleUser()));
    response.andExpect(status().isOk());
    verify(categoryService).findCategories(new CategorySearchFilter("query", null, 1, 2, 3, 4, true, true),
                                           SIMPLE_USER,
                                           Locale.ENGLISH);

    response =
             mockMvc.perform(get("/categories/search?query=query&ownerId=1&parentId=2&offset=3&limit=4&linkPermission=true&sortByName=true"));
    response.andExpect(status().isUnauthorized());
  }

  @Test
  public void canEditIsTrue() throws Exception {
    when(categoryService.canEdit(anyLong(), eq(SIMPLE_USER))).thenReturn(true);
    ResultActions response = mockMvc.perform(get(CAN_EDIT_CATEGORY_PATH).with(testSimpleUser()));
    response.andExpect(status().isOk())
            .andExpect(content().string("true"));
  }

  @Test
  public void canEditIsFalse() throws Exception {
    when(categoryService.canEdit(anyLong(), eq(SIMPLE_USER))).thenReturn(false);
    ResultActions response = mockMvc.perform(get(CAN_EDIT_CATEGORY_PATH).with(testSimpleUser()));
    response.andExpect(status().isOk())
            .andExpect(content().string("false"));
  }

  @Test
  public void canLinkIsTrue() throws Exception {
    when(categoryService.canManageLink(anyLong(), eq(SIMPLE_USER))).thenReturn(true);
    ResultActions response = mockMvc.perform(get(CAN_LINK_CATEGORY_PATH).with(testSimpleUser()));
    response.andExpect(status().isOk())
            .andExpect(content().string("true"));
  }

  @Test
  public void canLinkIsFalse() throws Exception {
    when(categoryService.canManageLink(anyLong(), eq(SIMPLE_USER))).thenReturn(false);
    ResultActions response = mockMvc.perform(get(CAN_LINK_CATEGORY_PATH).with(testSimpleUser()));
    response.andExpect(status().isOk())
            .andExpect(content().string("false"));
  }

  @Test
  public void createCategory() throws Exception {
    Category category = new Category();
    when(categoryService.createCategory(any(), eq(SIMPLE_USER))).thenReturn(category);
    ResultActions response = mockMvc.perform(post(CATEGORIES_PATH).content(toJsonString(category))
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .with(testSimpleUser()));
    response.andExpect(status().isOk());
    verify(categoryService).createCategory(category, SIMPLE_USER);
  }

  @Test
  public void createCategoryThrowsIllegalArgumentException() throws Exception {
    Category category = new Category();
    when(categoryService.createCategory(any(), eq(SIMPLE_USER))).thenThrow(IllegalArgumentException.class);
    ResultActions response = mockMvc.perform(post(CATEGORIES_PATH).content(toJsonString(category))
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .with(testSimpleUser()));
    response.andExpect(status().isBadRequest());
    verify(categoryService).createCategory(category, SIMPLE_USER);
  }

  @Test
  public void createCategoryThrowsObjectNotFoundException() throws Exception {
    Category category = new Category();
    when(categoryService.createCategory(any(), eq(SIMPLE_USER))).thenThrow(ObjectNotFoundException.class);
    ResultActions response = mockMvc.perform(post(CATEGORIES_PATH).content(toJsonString(category))
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .with(testSimpleUser()));
    response.andExpect(status().isNotFound());
    verify(categoryService).createCategory(category, SIMPLE_USER);
  }

  @Test
  public void createCategoryThrowsIllegalAccessException() throws Exception {
    Category category = new Category();
    when(categoryService.createCategory(any(), eq(SIMPLE_USER))).thenThrow(IllegalAccessException.class);
    ResultActions response = mockMvc.perform(post(CATEGORIES_PATH).content(toJsonString(category))
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .with(testSimpleUser()));
    response.andExpect(status().isForbidden());
    verify(categoryService).createCategory(category, SIMPLE_USER);
  }

  @Test
  public void createCategoryThrowsObjectAlreadyExistsException() throws Exception {
    Category category = new Category();
    when(categoryService.createCategory(any(), eq(SIMPLE_USER))).thenThrow(ObjectAlreadyExistsException.class);
    ResultActions response = mockMvc.perform(post(CATEGORIES_PATH).content(toJsonString(category))
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .with(testSimpleUser()));
    response.andExpect(status().isConflict());
    verify(categoryService).createCategory(category, SIMPLE_USER);
  }

  @Test
  public void updateCategory() throws Exception {
    Category category = new Category();
    when(categoryService.updateCategory(any(), eq(SIMPLE_USER))).thenReturn(category);
    ResultActions response = mockMvc.perform(put(DELETE_CATEGORY_PATH).content(toJsonString(category))
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .with(testSimpleUser()));
    response.andExpect(status().isOk());
    verify(categoryService).updateCategory(category, SIMPLE_USER);
  }

  @Test
  public void updateCategoryThrowsIllegalArgumentException() throws Exception {
    Category category = new Category();
    when(categoryService.updateCategory(any(), eq(SIMPLE_USER))).thenThrow(IllegalArgumentException.class);
    ResultActions response = mockMvc.perform(put(DELETE_CATEGORY_PATH).content(toJsonString(category))
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .with(testSimpleUser()));
    response.andExpect(status().isBadRequest());
    verify(categoryService).updateCategory(category, SIMPLE_USER);
  }

  @Test
  public void updateCategoryThrowsObjectNotFoundException() throws Exception {
    Category category = new Category();
    when(categoryService.updateCategory(any(), eq(SIMPLE_USER))).thenThrow(ObjectNotFoundException.class);
    ResultActions response = mockMvc.perform(put(DELETE_CATEGORY_PATH).content(toJsonString(category))
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .with(testSimpleUser()));
    response.andExpect(status().isNotFound());
    verify(categoryService).updateCategory(category, SIMPLE_USER);
  }

  @Test
  public void updateCategoryThrowsIllegalAccessException() throws Exception {
    Category category = new Category();
    when(categoryService.updateCategory(any(), eq(SIMPLE_USER))).thenThrow(IllegalAccessException.class);
    ResultActions response = mockMvc.perform(put(DELETE_CATEGORY_PATH).content(toJsonString(category))
                                                                      .contentType(MediaType.APPLICATION_JSON)
                                                                      .with(testSimpleUser()));
    response.andExpect(status().isForbidden());
    verify(categoryService).updateCategory(category, SIMPLE_USER);
  }

  @Test
  public void deleteCategory() throws Exception {
    ResultActions response = mockMvc.perform(delete(DELETE_CATEGORY_PATH).with(testSimpleUser()));
    response.andExpect(status().isOk());
    verify(categoryService).deleteCategory(1l, SIMPLE_USER);
  }

  @Test
  public void deleteCategoryThrowsIllegalArgumentException() throws Exception {
    when(categoryService.deleteCategory(anyLong(), eq(SIMPLE_USER))).thenThrow(IllegalArgumentException.class);
    ResultActions response = mockMvc.perform(delete(DELETE_CATEGORY_PATH).with(testSimpleUser()));
    response.andExpect(status().isBadRequest());
    verify(categoryService).deleteCategory(1l, SIMPLE_USER);
  }

  @Test
  public void deleteCategoryThrowsObjectNotFoundException() throws Exception {
    when(categoryService.deleteCategory(anyLong(), eq(SIMPLE_USER))).thenThrow(ObjectNotFoundException.class);
    ResultActions response = mockMvc.perform(delete(DELETE_CATEGORY_PATH).with(testSimpleUser()));
    response.andExpect(status().isNotFound());
    verify(categoryService).deleteCategory(1l, SIMPLE_USER);
  }

  @Test
  public void deleteCategoryThrowsIllegalAccessException() throws Exception {
    when(categoryService.deleteCategory(anyLong(), eq(SIMPLE_USER))).thenThrow(IllegalAccessException.class);
    ResultActions response = mockMvc.perform(delete(DELETE_CATEGORY_PATH).with(testSimpleUser()));
    response.andExpect(status().isForbidden());
    verify(categoryService).deleteCategory(1l, SIMPLE_USER);
  }

  private RequestPostProcessor testSimpleUser() {
    return user(SIMPLE_USER).password(TEST_PASSWORD)
                            .authorities(new SimpleGrantedAuthority("users"));
  }

}
