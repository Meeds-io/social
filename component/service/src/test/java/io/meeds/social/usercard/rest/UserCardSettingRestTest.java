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
package io.meeds.social.usercard.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import io.meeds.social.usercard.model.UserCardSettings;
import io.meeds.social.usercard.service.UserCardSettingService;
import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;

import jakarta.servlet.Filter;

@SpringBootTest(classes = { UserCardSettingRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class UserCardSettingRestTest {

  private static final String           SIMPLE_USER   = "simple";

  private static final String           ADMIN_USER    = "admin";

  private static final String           TEST_PASSWORD = "testPassword";

  private static final String           SETTINGS_BODY =
                                                      "{\"firstField\":\"position\",\"secondField\":\"team\",\"thirdField\":\"city\",\"displayedPhone\":null,\"displayedEmail\":\"workEmail\"}";

  private static final UserCardSettings SETTINGS      = new UserCardSettings("position", "team", "city", null, "workEmail");

  @Autowired
  private SecurityFilterChain           filterChain;

  @Autowired
  private WebApplicationContext         context;

  @MockitoBean
  private UserCardSettingService        userCardSettingService;

  private MockMvc                       mockMvc;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
  }

  @Test
  public void saveSettingsAnonymously() throws Exception {
    mockMvc.perform(put("/user-card/settings").contentType(MediaType.APPLICATION_JSON).content(SETTINGS_BODY))
           .andExpect(status().isForbidden());
    verify(userCardSettingService, never()).saveSettings(any(), anyString());
  }

  @Test
  public void saveSettingsWithSimpleUser() throws Exception {
    mockMvc.perform(put("/user-card/settings").contentType(MediaType.APPLICATION_JSON)
                                              .content(SETTINGS_BODY)
                                              .with(testSimpleUser()))
           .andExpect(status().isForbidden());
    verify(userCardSettingService, never()).saveSettings(any(), anyString());
  }

  @Test
  public void saveSettingsWithAdministrator() throws Exception {
    mockMvc.perform(put("/user-card/settings").contentType(MediaType.APPLICATION_JSON)
                                              .content(SETTINGS_BODY)
                                              .with(testAdminUser()))
           .andExpect(status().isOk());
    verify(userCardSettingService).saveSettings(SETTINGS, ADMIN_USER);
  }

  @Test
  public void saveSettingsWithInvalidProperty() throws Exception {
    doThrow(new IllegalArgumentException(UserCardSettingService.INVALID_PROPERTY_MESSAGE)).when(userCardSettingService)
                                                                                          .saveSettings(SETTINGS, ADMIN_USER);
    mockMvc.perform(put("/user-card/settings").contentType(MediaType.APPLICATION_JSON)
                                              .content(SETTINGS_BODY)
                                              .with(testAdminUser()))
           .andExpect(status().isBadRequest());
  }

  @Test
  public void saveSettingsWhenServiceRefuses() throws Exception {
    doThrow(new IllegalAccessException()).when(userCardSettingService).saveSettings(SETTINGS, ADMIN_USER);
    mockMvc.perform(put("/user-card/settings").contentType(MediaType.APPLICATION_JSON)
                                              .content(SETTINGS_BODY)
                                              .with(testAdminUser()))
           .andExpect(status().isForbidden());
  }

  private RequestPostProcessor testSimpleUser() {
    return user(SIMPLE_USER).password(TEST_PASSWORD)
                            .authorities(new SimpleGrantedAuthority("users"));
  }

  private RequestPostProcessor testAdminUser() {
    return user(ADMIN_USER).password(TEST_PASSWORD)
                           .authorities(new SimpleGrantedAuthority("users"), new SimpleGrantedAuthority("administrators"));
  }

}
