/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.hidden.rest;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureWebMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.social.hidden.model.HiddenApplication;
import io.meeds.social.hidden.service.HiddenApplicationService;
import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;

import jakarta.servlet.Filter;

@SpringBootTest(classes = { HiddenApplicationRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class HiddenApplicationRestTest {

  private static final String      REST_PATH     = "/hidden-applications";

  private static final String      SIMPLE_USER   = "simple";

  private static final String      TEST_PASSWORD = "testPassword";

  private static final String      TYPE          = "links";

  private static final String      NAME          = "Untitled-1-abc";

  @Autowired
  private SecurityFilterChain      filterChain;

  @Autowired
  private WebApplicationContext    context;

  @MockitoBean
  private HiddenApplicationService hiddenApplicationService;

  private MockMvc                  mockMvc;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
  }

  @Test
  public void getHiddenApplicationsAnonymouslyIsForbidden() throws Exception {
    mockMvc.perform(get(REST_PATH)).andExpect(status().isForbidden());
    verify(hiddenApplicationService, never()).getHiddenApplications(nullable(String.class), nullable(String.class));
  }

  @Test
  public void getHiddenApplicationsOfTheCurrentUser() throws Exception {
    HiddenApplication hiddenApplication = new HiddenApplication(TYPE, NAME, "Premiers pas", "portal::dw::overview", true);
    hiddenApplication.setPageTitle("Overview");
    when(hiddenApplicationService.getHiddenApplications(SIMPLE_USER, "fr")).thenReturn(List.of(hiddenApplication));

    mockMvc.perform(get(REST_PATH + "?lang=fr").with(testSimpleUser()))
           .andExpect(status().isOk())
           .andExpect(header().string("Cache-Control", "no-store"))
           .andExpect(jsonPath("$[0].type").value(TYPE))
           .andExpect(jsonPath("$[0].name").value(NAME))
           .andExpect(jsonPath("$[0].title").value("Premiers pas"))
           .andExpect(jsonPath("$[0].pageTitle").value("Overview"));
  }

  @Test
  public void hideAnonymouslyIsForbidden() throws Exception {
    mockMvc.perform(put(REST_PATH + "/" + TYPE + "/" + NAME)).andExpect(status().isForbidden());
    verify(hiddenApplicationService, never()).hide(nullable(String.class), nullable(String.class), nullable(String.class));
  }

  @Test
  public void hideForTheCurrentUser() throws Exception {
    mockMvc.perform(put(REST_PATH + "/" + TYPE + "/" + NAME).with(testSimpleUser())).andExpect(status().isNoContent());
    verify(hiddenApplicationService).hide(TYPE, NAME, SIMPLE_USER);
  }

  @Test
  public void hideMapsTheRefusalsOfTheService() throws Exception {
    doThrow(new IllegalArgumentException(HiddenApplicationService.HIDE_NOT_ALLOWED)).when(hiddenApplicationService)
                                                                                     .hide(TYPE, "notAllowed", SIMPLE_USER);
    doThrow(new ObjectNotFoundException("")).when(hiddenApplicationService).hide(TYPE, "deleted", SIMPLE_USER);

    // The 400 carries the refusal's message code, which the client shows through its label
    mockMvc.perform(put(REST_PATH + "/" + TYPE + "/notAllowed").with(testSimpleUser()))
           .andExpect(status().isBadRequest())
           .andExpect(content().string(HiddenApplicationService.HIDE_NOT_ALLOWED));
    // A window the user can't view is not found, as one that doesn't exist
    mockMvc.perform(put(REST_PATH + "/" + TYPE + "/deleted").with(testSimpleUser())).andExpect(status().isNotFound());
  }

  @Test
  public void showAnonymouslyIsForbidden() throws Exception {
    mockMvc.perform(delete(REST_PATH + "/" + TYPE + "/" + NAME)).andExpect(status().isForbidden());
    verify(hiddenApplicationService, never()).show(nullable(String.class), nullable(String.class), nullable(String.class));
  }

  @Test
  public void showForTheCurrentUser() throws Exception {
    mockMvc.perform(delete(REST_PATH + "/" + TYPE + "/" + NAME).with(testSimpleUser())).andExpect(status().isNoContent());
    verify(hiddenApplicationService).show(TYPE, NAME, SIMPLE_USER);
  }

  private RequestPostProcessor testSimpleUser() {
    return user(SIMPLE_USER).password(TEST_PASSWORD).authorities(new SimpleGrantedAuthority("users"));
  }

}
