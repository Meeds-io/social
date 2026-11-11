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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureWebMvc;
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

import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;
import io.meeds.web.security.service.ApiKeyService;

import jakarta.servlet.Filter;
import lombok.SneakyThrows;

@SpringBootTest(classes = { ApiKeyRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class ApiKeyRestTest {

  private static final String   USERNAME = "john";

  private MockMvc               mockMvc;

  @Autowired
  private SecurityFilterChain   filterChain;

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private ApiKeyService         apiKeyService;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
  }

  @Test
  @SneakyThrows
  public void testGetPasswordWithACodeOfTheAccount() {
    when(apiKeyService.getPassword(USERNAME, "email", "123456", false)).thenReturn("secret");
    mockMvc.perform(post("/apiKey").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                   .param("method", "email")
                                   .param("code", "123456")
                                   .with(testUser()))
           .andExpect(status().isOk())
           .andExpect(content().string("secret"));
  }

  /**
   * A code of the email change goes to an address the caller chooses: it never
   * reveals the password, and the code is not even checked.
   */
  @Test
  @SneakyThrows
  public void testGetPasswordRefusesAMethodThatDoesNotProveTheAccount() {
    mockMvc.perform(post("/apiKey").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                   .param("method", "emailChange")
                                   .param("code", "123456:someone@example.org")
                                   .with(testUser()))
           .andExpect(status().isForbidden());
    verify(apiKeyService, never()).getPassword(anyString(), anyString(), anyString(), anyBoolean());
  }

  private RequestPostProcessor testUser() {
    return user(USERNAME).authorities(new SimpleGrantedAuthority("users"));
  }

}
