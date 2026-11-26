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
package io.meeds.social.logging.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.portal.logging.constant.LoggingLevel;
import io.meeds.portal.logging.model.LoggerLevel;
import io.meeds.portal.logging.model.LoggerLevelPage;
import io.meeds.portal.logging.model.LoggingLevelStatus;
import io.meeds.portal.logging.service.LoggingLevelConfiguration;
import io.meeds.portal.logging.service.LoggingLevelService;
import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;

import jakarta.servlet.Filter;

@SpringBootTest(classes = { LoggingLevelRest.class, LoggingLevelConfiguration.class, PortalAuthenticationManager.class, },
                properties = "meeds.logging.levels.roles=administrators, loggingAdmins")
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class LoggingLevelRestTest {

  private static final String   LOGGERS_URL = "/logging/loggers";

  private static final String   STATUS_URL  = "/logging/status";

  private static final String   LOGGER_URL  = "/logging/loggers/org.exoplatform.portal";

  private static final String   BODY        = "{\"level\":\"DEBUG\"}";

  @Autowired
  private SecurityFilterChain   filterChain;

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private LoggingLevelService   loggingLevelService;

  private MockMvc               mockMvc;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
  }

  @Test
  public void everyEndpointIsRefusedToAnonymous() throws Exception {
    mockMvc.perform(get(LOGGERS_URL)).andExpect(status().isForbidden());
    mockMvc.perform(get(STATUS_URL)).andExpect(status().isForbidden());
    mockMvc.perform(put(LOGGER_URL).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isForbidden());
    mockMvc.perform(delete(LOGGER_URL)).andExpect(status().isForbidden());
    mockMvc.perform(delete(LOGGERS_URL)).andExpect(status().isForbidden());
    verify(loggingLevelService, never()).setLevel(anyString(), anyString(), anyString());
    verify(loggingLevelService, never()).resetAll(anyString());
  }

  @Test
  public void everyEndpointIsRefusedToASimpleUser() throws Exception {
    mockMvc.perform(get(LOGGERS_URL).with(simpleUser())).andExpect(status().isForbidden());
    mockMvc.perform(get(STATUS_URL).with(simpleUser())).andExpect(status().isForbidden());
    mockMvc.perform(put(LOGGER_URL).contentType(MediaType.APPLICATION_JSON).content(BODY).with(simpleUser()))
           .andExpect(status().isForbidden());
    mockMvc.perform(delete(LOGGER_URL).with(simpleUser())).andExpect(status().isForbidden());
    mockMvc.perform(delete(LOGGERS_URL).with(simpleUser())).andExpect(status().isForbidden());
    verify(loggingLevelService, never()).setLevel(anyString(), anyString(), anyString());
    verify(loggingLevelService, never()).resetAll(anyString());
  }

  @Test
  public void getLoggers() throws Exception {
    when(loggingLevelService.getLoggers("org", true, 5, 10)).thenReturn(new LoggerLevelPage(List.of(new LoggerLevel("org.exoplatform",
                                                                                                                  LoggingLevel.DEBUG,
                                                                                                                  LoggingLevel.DEBUG,
                                                                                                                  LoggingLevel.INFO,
                                                                                                                  true,
                                                                                                                  1_732_540_000_000L,
                                                                                                                  false)),
                                                                                                5,
                                                                                                10,
                                                                                                6));
    mockMvc.perform(get(LOGGERS_URL).param("query", "org").param("explicitOnly", "true").param("offset", "5").param("limit", "10")
                                    .with(administrator()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.total").value(6))
           .andExpect(jsonPath("$.loggers[0].name").value("org.exoplatform"))
           .andExpect(jsonPath("$.loggers[0].level").value("DEBUG"))
           .andExpect(jsonPath("$.loggers[0].configuredLevel").value("INFO"))
           .andExpect(jsonPath("$.loggers[0].overridden").value(true))
           .andExpect(jsonPath("$.loggers[0].revertsAt").value(1_732_540_000_000L))
           .andExpect(jsonPath("$.loggers[0].editable").value(false));
  }

  @Test
  public void getLoggersWithDefaults() throws Exception {
    when(loggingLevelService.getLoggers(any(), anyBoolean(), anyInt(), anyInt())).thenReturn(new LoggerLevelPage(List.of(), 0, 20, 0));
    mockMvc.perform(get(LOGGERS_URL).with(administrator())).andExpect(status().isOk());
    verify(loggingLevelService).getLoggers(null, false, 0, 20);
  }

  @Test
  public void setLevel() throws Exception {
    mockMvc.perform(put(LOGGER_URL).contentType(MediaType.APPLICATION_JSON).content(BODY).with(administrator()))
           .andExpect(status().isOk());
    verify(loggingLevelService).setLevel("org.exoplatform.portal", "DEBUG", "admin");
  }

  @Test
  public void setLevelWithAnInvalidValue() throws Exception {
    doThrow(new IllegalArgumentException("logging.level.invalid")).when(loggingLevelService).setLevel(eq("org.exoplatform.portal"), anyString(), eq("admin"));
    mockMvc.perform(put(LOGGER_URL).contentType(MediaType.APPLICATION_JSON).content("{\"level\":\"LOUD\"}").with(administrator()))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").value("logging.level.invalid"));
  }

  @Test
  public void resetLevel() throws Exception {
    mockMvc.perform(delete(LOGGER_URL).with(administrator())).andExpect(status().isOk());
    verify(loggingLevelService).resetLevel("org.exoplatform.portal", "admin");
  }

  @Test
  public void resetLevelOfALoggerWithoutOverride() throws Exception {
    doThrow(new ObjectNotFoundException("logging.override.notFound")).when(loggingLevelService).resetLevel("org.exoplatform.portal", "admin");
    mockMvc.perform(delete(LOGGER_URL).with(administrator()))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("logging.override.notFound"));
  }

  @Test
  public void resetAll() throws Exception {
    mockMvc.perform(delete(LOGGERS_URL).with(administrator())).andExpect(status().isOk());
    verify(loggingLevelService).resetAll("admin");
  }

  @Test
  public void aUserOfTheConfiguredRoleIsAllowed() throws Exception {
    when(loggingLevelService.getLoggers(any(), anyBoolean(), anyInt(), anyInt())).thenReturn(new LoggerLevelPage(List.of(), 0, 20, 0));
    mockMvc.perform(get(LOGGERS_URL).with(user("ops").password("testPassword").authorities(new SimpleGrantedAuthority("loggingAdmins"))))
           .andExpect(status().isOk());
    mockMvc.perform(delete(LOGGERS_URL).with(user("ops").password("testPassword").authorities(new SimpleGrantedAuthority("loggingAdmins"))))
           .andExpect(status().isOk());
    verify(loggingLevelService).resetAll("ops");
  }

  @Test
  public void aRoleThatIsNotConfiguredIsRefusedOnEveryEndpointEvenIfItIsAGroupOfTheUser() throws Exception {
    mockMvc.perform(get(STATUS_URL).with(otherRole())).andExpect(status().isForbidden());
    mockMvc.perform(get(LOGGERS_URL).with(otherRole())).andExpect(status().isForbidden());
    mockMvc.perform(put(LOGGER_URL).contentType(MediaType.APPLICATION_JSON).content(BODY).with(otherRole())).andExpect(status().isForbidden());
    mockMvc.perform(delete(LOGGER_URL).with(otherRole())).andExpect(status().isForbidden());
    mockMvc.perform(delete(LOGGERS_URL).with(otherRole())).andExpect(status().isForbidden());
    verify(loggingLevelService, never()).setLevel(anyString(), anyString(), anyString());
    verify(loggingLevelService, never()).resetAll(anyString());
  }

  @Test
  public void theStatusTellsWhatThePageMayDo() throws Exception {
    when(loggingLevelService.getStatus()).thenReturn(new LoggingLevelStatus(true, true, 45));
    mockMvc.perform(get(STATUS_URL).with(administrator()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.enabled").value(true))
           .andExpect(jsonPath("$.readOnly").value(true))
           .andExpect(jsonPath("$.revertAfterMinutes").value(45));
  }

  @Test
  public void whenTheFeatureIsDisabledTheListIsRefusedWithItsCode() throws Exception {
    when(loggingLevelService.getLoggers(any(), anyBoolean(), anyInt(), anyInt())).thenThrow(new IllegalAccessException("logging.disabled"));
    mockMvc.perform(get(LOGGERS_URL).with(administrator()))
           .andExpect(status().isForbidden())
           .andExpect(jsonPath("$.message").value("logging.disabled"));
  }

  @Test
  public void whenTheFeatureIsReadOnlyEveryChangeIsRefusedWithItsCode() throws Exception {
    doThrow(new IllegalAccessException("logging.readOnly")).when(loggingLevelService).setLevel(anyString(), anyString(), anyString());
    doThrow(new IllegalAccessException("logging.readOnly")).when(loggingLevelService).resetLevel(anyString(), anyString());
    doThrow(new IllegalAccessException("logging.readOnly")).when(loggingLevelService).resetAll(anyString());
    mockMvc.perform(put(LOGGER_URL).contentType(MediaType.APPLICATION_JSON).content(BODY).with(administrator()))
           .andExpect(status().isForbidden())
           .andExpect(jsonPath("$.message").value("logging.readOnly"));
    mockMvc.perform(delete(LOGGER_URL).with(administrator()))
           .andExpect(status().isForbidden())
           .andExpect(jsonPath("$.message").value("logging.readOnly"));
    mockMvc.perform(delete(LOGGERS_URL).with(administrator()))
           .andExpect(status().isForbidden())
           .andExpect(jsonPath("$.message").value("logging.readOnly"));
  }

  private RequestPostProcessor otherRole() {
    return user("other").password("testPassword").authorities(new SimpleGrantedAuthority("managers"));
  }

  private RequestPostProcessor simpleUser() {
    return user("simple").password("testPassword").authorities(new SimpleGrantedAuthority("users"));
  }

  private RequestPostProcessor administrator() {
    return user("admin").password("testPassword").authorities(new SimpleGrantedAuthority("administrators"));
  }

}
