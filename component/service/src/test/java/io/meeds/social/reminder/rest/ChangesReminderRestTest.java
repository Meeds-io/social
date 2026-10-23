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
package io.meeds.social.reminder.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import io.meeds.social.reminder.service.ChangesReminderService;
import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;

import jakarta.servlet.Filter;

@SpringBootTest(classes = { ChangesReminderRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class ChangesReminderRestTest {

  private static final String    SIMPLE_USER   = "simple";

  private static final String    TEST_PASSWORD = "testPassword";

  private static final String    REMINDER      = "notes-tree";

  @Autowired
  private SecurityFilterChain    filterChain;

  @Autowired
  private WebApplicationContext  context;

  @MockitoBean
  private ChangesReminderService changesReminderService;

  private MockMvc                mockMvc;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
  }

  @Test
  public void isAcknowledgedAnonymously() throws Exception {
    mockMvc.perform(get("/changes-reminder/" + REMINDER)).andExpect(status().isForbidden());
    verify(changesReminderService, never()).isAcknowledged(anyString(), anyString());
  }

  @Test
  public void isAcknowledgedReadsTheCallerValue() throws Exception {
    when(changesReminderService.isAcknowledged(SIMPLE_USER, REMINDER)).thenReturn(true);
    mockMvc.perform(get("/changes-reminder/" + REMINDER).with(testSimpleUser()))
           .andExpect(status().isOk())
           .andExpect(header().string("Cache-Control", "no-store"))
           .andExpect(content().string("true"));
  }

  @Test
  public void isAcknowledgedWithInvalidName() throws Exception {
    when(changesReminderService.isAcknowledged(SIMPLE_USER, "bad")).thenThrow(new IllegalArgumentException(ChangesReminderService.INVALID_NAME_MESSAGE));
    mockMvc.perform(get("/changes-reminder/bad").with(testSimpleUser())).andExpect(status().isBadRequest());
  }

  @Test
  public void acknowledgeAnonymously() throws Exception {
    mockMvc.perform(put("/changes-reminder/" + REMINDER)).andExpect(status().isForbidden());
    verify(changesReminderService, never()).acknowledge(anyString(), anyString());
  }

  @Test
  public void acknowledgeForTheCaller() throws Exception {
    mockMvc.perform(put("/changes-reminder/" + REMINDER).with(testSimpleUser())).andExpect(status().isOk());
    verify(changesReminderService).acknowledge(SIMPLE_USER, REMINDER);
  }

  @Test
  public void acknowledgeWithInvalidName() throws Exception {
    doThrow(new IllegalArgumentException(ChangesReminderService.INVALID_NAME_MESSAGE)).when(changesReminderService)
                                                                                      .acknowledge(SIMPLE_USER, "bad");
    mockMvc.perform(put("/changes-reminder/bad").with(testSimpleUser())).andExpect(status().isBadRequest());
  }

  private RequestPostProcessor testSimpleUser() {
    return user(SIMPLE_USER).password(TEST_PASSWORD)
                            .authorities(new SimpleGrantedAuthority("users"));
  }

}
