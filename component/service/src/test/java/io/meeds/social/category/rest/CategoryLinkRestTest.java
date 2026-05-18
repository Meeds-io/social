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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.category.model.CategoryObject;
import io.meeds.social.category.service.CategoryLinkService;
import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;

import jakarta.servlet.Filter;

@SpringBootTest(classes = { CategoryLinkRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class CategoryLinkRestTest {

  private static final String   CATEGORIES_PATH        = "/category/links/1";

  private static final String   SIMPLE_USER            = "simple";

  private static final String   TEST_PASSWORD          = "testPassword";

  @Autowired
  private SecurityFilterChain   filterChain;

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private CategoryLinkService   categoryLinkService;

  private MockMvc               mockMvc;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
  }

  @Test
  public void link() throws Exception {
    CategoryObject object = new CategoryObject();
    ResultActions response = mockMvc.perform(post(CATEGORIES_PATH).content(toJsonString(object))
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .with(testSimpleUser()));
    response.andExpect(status().isOk());
    verify(categoryLinkService).link(1l, object, SIMPLE_USER);
  }

  @Test
  public void linkWhenThrowIllegalArgumentException() throws Exception {
    CategoryObject object = new CategoryObject();
    doThrow(IllegalArgumentException.class).when(categoryLinkService).link(1l, object, SIMPLE_USER);
    ResultActions response = mockMvc.perform(post(CATEGORIES_PATH).content(toJsonString(object))
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .with(testSimpleUser()));
    response.andExpect(status().isBadRequest());
  }

  @Test
  public void linkWhenThrowObjectNotFoundException() throws Exception {
    CategoryObject object = new CategoryObject();
    doThrow(ObjectNotFoundException.class).when(categoryLinkService).link(1l, object, SIMPLE_USER);
    ResultActions response = mockMvc.perform(post(CATEGORIES_PATH).content(toJsonString(object))
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .with(testSimpleUser()));
    response.andExpect(status().isNotFound());
  }

  @Test
  public void linkWhenThrowIllegalAccessException() throws Exception {
    CategoryObject object = new CategoryObject();
    doThrow(IllegalAccessException.class).when(categoryLinkService).link(1l, object, SIMPLE_USER);
    ResultActions response = mockMvc.perform(post(CATEGORIES_PATH).content(toJsonString(object))
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .with(testSimpleUser()));
    response.andExpect(status().isForbidden());
  }

  @Test
  public void linkWhenThrowObjectAlreadyExistsException() throws Exception {
    CategoryObject object = new CategoryObject();
    doThrow(ObjectAlreadyExistsException.class).when(categoryLinkService).link(1l, object, SIMPLE_USER);
    ResultActions response = mockMvc.perform(post(CATEGORIES_PATH).content(toJsonString(object))
                                                                  .contentType(MediaType.APPLICATION_JSON)
                                                                  .with(testSimpleUser()));
    response.andExpect(status().isConflict());
  }

  @Test
  public void unlink() throws Exception {
    CategoryObject object = new CategoryObject();
    ResultActions response = mockMvc.perform(delete(CATEGORIES_PATH).content(toJsonString(object))
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .with(testSimpleUser()));
    response.andExpect(status().isOk());
    verify(categoryLinkService).unlink(1l, object, SIMPLE_USER);
  }

  @Test
  public void unlinkWhenThrowIllegalArgumentException() throws Exception {
    CategoryObject object = new CategoryObject();
    doThrow(IllegalArgumentException.class).when(categoryLinkService).unlink(1l, object, SIMPLE_USER);
    ResultActions response = mockMvc.perform(delete(CATEGORIES_PATH).content(toJsonString(object))
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .with(testSimpleUser()));
    response.andExpect(status().isBadRequest());
  }

  @Test
  public void unlinkWhenThrowObjectNotFoundException() throws Exception {
    CategoryObject object = new CategoryObject();
    doThrow(ObjectNotFoundException.class).when(categoryLinkService).unlink(1l, object, SIMPLE_USER);
    ResultActions response = mockMvc.perform(delete(CATEGORIES_PATH).content(toJsonString(object))
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .with(testSimpleUser()));
    response.andExpect(status().isNotFound());
  }

  @Test
  public void unlinkWhenThrowIllegalAccessException() throws Exception {
    CategoryObject object = new CategoryObject();
    doThrow(IllegalAccessException.class).when(categoryLinkService).unlink(1l, object, SIMPLE_USER);
    ResultActions response = mockMvc.perform(delete(CATEGORIES_PATH).content(toJsonString(object))
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .with(testSimpleUser()));
    response.andExpect(status().isForbidden());
  }

  private RequestPostProcessor testSimpleUser() {
    return user(SIMPLE_USER).password(TEST_PASSWORD)
                            .authorities(new SimpleGrantedAuthority("users"));
  }

}
