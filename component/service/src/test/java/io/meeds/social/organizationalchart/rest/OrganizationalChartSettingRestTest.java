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
package io.meeds.social.organizationalchart.rest;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import io.meeds.social.organizationalchart.service.OrganizationalChartSettingService;
import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;

import jakarta.servlet.Filter;

@SpringBootTest(classes = { OrganizationalChartSettingRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class OrganizationalChartSettingRestTest {

  private static final String               SIMPLE_USER   = "simple";

  private static final String               TEST_PASSWORD = "testPassword";

  private static final String               SETTINGS_URL  = "/organizational-chart/125/settings";

  private static final String               SETTINGS_BODY = "{\"centerUser\":\"12\",\"hasHeaderTitle\":true}";

  @Autowired
  private SecurityFilterChain               filterChain;

  @Autowired
  private WebApplicationContext             context;

  @MockitoBean
  private OrganizationalChartSettingService organizationalChartSettingService;

  private MockMvc                           mockMvc;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
  }

  @Test
  public void saveSettingsAnonymously() throws Exception {
    mockMvc.perform(put(SETTINGS_URL).contentType(MediaType.APPLICATION_JSON).content(SETTINGS_BODY))
           .andExpect(status().isForbidden());
    verify(organizationalChartSettingService, never()).saveSettings(anyString(), anyString(), anyBoolean(), anyString());
  }

  @Test
  public void saveSettingsForTheCaller() throws Exception {
    mockMvc.perform(put(SETTINGS_URL).contentType(MediaType.APPLICATION_JSON).content(SETTINGS_BODY).with(testSimpleUser()))
           .andExpect(status().isOk());
    verify(organizationalChartSettingService).saveSettings("125", "12", true, SIMPLE_USER);
  }

  @Test
  public void saveSettingsWhenNotAllowed() throws Exception {
    doThrow(new IllegalAccessException()).when(organizationalChartSettingService).saveSettings("125", "12", true, SIMPLE_USER);
    mockMvc.perform(put(SETTINGS_URL).contentType(MediaType.APPLICATION_JSON).content(SETTINGS_BODY).with(testSimpleUser()))
           .andExpect(status().isForbidden());
  }

  @Test
  public void saveSettingsWithInvalidValue() throws Exception {
    doThrow(new IllegalArgumentException(OrganizationalChartSettingService.INVALID_CENTER_USER_MESSAGE)).when(organizationalChartSettingService)
                                                                                                        .saveSettings("125",
                                                                                                                      "12",
                                                                                                                      true,
                                                                                                                      SIMPLE_USER);
    mockMvc.perform(put(SETTINGS_URL).contentType(MediaType.APPLICATION_JSON).content(SETTINGS_BODY).with(testSimpleUser()))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").value(OrganizationalChartSettingService.INVALID_CENTER_USER_MESSAGE));
  }

  private RequestPostProcessor testSimpleUser() {
    return user(SIMPLE_USER).password(TEST_PASSWORD)
                            .authorities(new SimpleGrantedAuthority("users"));
  }

}
