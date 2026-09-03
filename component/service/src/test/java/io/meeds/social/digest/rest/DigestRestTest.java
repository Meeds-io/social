/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.social.digest.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import io.meeds.commons.digest.DigestService;
import io.meeds.commons.digest.model.DigestUserSettings;
import io.meeds.commons.digest.service.DigestLabelResolver;
import io.meeds.social.timezone.service.UserTimeZoneService;
import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;

import jakarta.servlet.Filter;

@SpringBootTest(classes = { DigestRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class DigestRestTest {

  private static final String   SIMPLE_USER   = "simple";

  private static final String   ADMIN_USER    = "admin";

  private static final String   TEST_PASSWORD = "testPassword";

  @Autowired
  private SecurityFilterChain   filterChain;

  @Autowired
  private WebApplicationContext context;

  @MockBean
  private DigestService         digestService;

  @MockBean
  private DigestLabelResolver   labelResolver;

  @MockBean
  private UserTimeZoneService   userTimeZoneService;

  private MockMvc               mockMvc;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
  }

  @Test
  public void getSettingsAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(get("/notifications/digest/settings"));
    response.andExpect(status().isForbidden());
  }

  @Test
  public void getSettingsWithUser() throws Exception {
    when(digestService.getUserSettings(anyString())).thenReturn(new DigestUserSettings(false, List.of(), false, List.of()));
    when(digestService.isDigestAllowed()).thenReturn(true);
    ResultActions response = mockMvc.perform(get("/notifications/digest/settings").with(testSimpleUser()));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$.digestAllowed").value(true));

    when(digestService.isDigestAllowed()).thenReturn(false);
    response = mockMvc.perform(get("/notifications/digest/settings").with(testSimpleUser()));
    response.andExpect(status().isOk())
            .andExpect(jsonPath("$.digestAllowed").value(false));
  }

  @Test
  public void saveDigestAllowedAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(patch("/notifications/digest/allowed").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                                                                   .content("allowed=true"));
    response.andExpect(status().isForbidden());
  }

  @Test
  public void saveDigestAllowedWithUser() throws Exception {
    ResultActions response = mockMvc.perform(patch("/notifications/digest/allowed").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                                                                   .content("allowed=true")
                                                                                   .with(testSimpleUser()));
    response.andExpect(status().isForbidden());
  }

  @Test
  public void saveDigestAllowedWithAdmin() throws Exception {
    // Same wire format as the real frontend call: form-encoded PATCH body
    ResultActions response = mockMvc.perform(patch("/notifications/digest/allowed").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                                                                   .content("allowed=true")
                                                                                   .with(testAdminUser()));
    response.andExpect(status().isOk());
    verify(digestService).saveDigestAllowed(true);

    response = mockMvc.perform(patch("/notifications/digest/allowed").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                                                     .content("allowed=false")
                                                                     .with(testAdminUser()));
    response.andExpect(status().isOk());
    verify(digestService).saveDigestAllowed(false);
  }

  private RequestPostProcessor testSimpleUser() {
    return user(SIMPLE_USER).password(TEST_PASSWORD)
                            .authorities(new SimpleGrantedAuthority("users"));
  }

  private RequestPostProcessor testAdminUser() {
    return user(ADMIN_USER).password(TEST_PASSWORD)
                           .authorities(new SimpleGrantedAuthority("administrators"));
  }

}
