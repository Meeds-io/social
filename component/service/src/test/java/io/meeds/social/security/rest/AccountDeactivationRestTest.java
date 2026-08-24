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
package io.meeds.social.security.rest;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

import io.meeds.social.security.service.AccountDeactivationService;
import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;

import jakarta.servlet.Filter;
import lombok.SneakyThrows;

@SpringBootTest(classes = { AccountDeactivationRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class AccountDeactivationRestTest {

  private static final String        USERNAME     = "john";

  private static final String        OTP_METHOD   = "accountDeactivationEmail";

  private static final String        OTP_CODE     = "12345";

  private static final String        REQUEST_BODY = """
      {"otpMethod": "accountDeactivationEmail", "otpCode": "12345", "deleteAccount": true}""";

  private MockMvc                    mockMvc;

  @Autowired
  private SecurityFilterChain        filterChain;

  @Autowired
  private WebApplicationContext      context;

  @MockitoBean
  private AccountDeactivationService accountDeactivationService;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
  }

  @Test
  @SneakyThrows
  public void testRequestDeactivationAnonymously() {
    mockMvc.perform(post("/account/deactivation").content(REQUEST_BODY)
                                                 .contentType(MediaType.APPLICATION_JSON))
           .andExpect(status().isForbidden());
    verify(accountDeactivationService, never()).requestDeactivation(anyString(), anyString(), anyString(), anyBoolean());
  }

  @Test
  @SneakyThrows
  public void testRequestDeactivationWhenNotAllowed() {
    doThrow(new IllegalStateException()).when(accountDeactivationService)
                                        .requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, true);
    mockMvc.perform(post("/account/deactivation").content(REQUEST_BODY)
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .with(testUser()))
           .andExpect(status().isForbidden());
  }

  @Test
  @SneakyThrows
  public void testRequestDeactivationWithWrongOtpCode() {
    doThrow(new IllegalAccessException()).when(accountDeactivationService)
                                         .requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, true);
    mockMvc.perform(post("/account/deactivation").content(REQUEST_BODY)
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .with(testUser()))
           .andExpect(status().isUnauthorized());
  }

  @Test
  @SneakyThrows
  public void testRequestDeactivationWithDeletion() {
    mockMvc.perform(post("/account/deactivation").content(REQUEST_BODY)
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .with(testUser()))
           .andExpect(status().isNoContent());
    verify(accountDeactivationService).requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, true);
  }

  @Test
  @SneakyThrows
  public void testRequestDeactivationWithoutDeletion() {
    mockMvc.perform(post("/account/deactivation").content("""
        {"otpMethod": "accountDeactivationEmail", "otpCode": "12345", "deleteAccount": false}""")
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .with(testUser()))
           .andExpect(status().isNoContent());
    verify(accountDeactivationService).requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, false);
  }

  private RequestPostProcessor testUser() {
    return user(USERNAME).authorities(new SimpleGrantedAuthority("users"));
  }

}
