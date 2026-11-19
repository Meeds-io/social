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
package io.meeds.social.space.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
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
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.SpaceStorageException;

import io.meeds.social.space.security.exception.SpaceSecurityChangeNotAppliedException;
import io.meeds.social.space.security.exception.SpaceSecurityRefusedException;
import io.meeds.social.space.security.model.SpaceSecuritySettings;
import io.meeds.social.space.security.service.SpaceSecurityService;
import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;

import jakarta.servlet.Filter;

@SpringBootTest(classes = { SpaceSecurityRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@RunWith(SpringRunner.class)
public class SpaceSecurityRestTest {

  private static final long     SPACE_ID      = 4L;

  private static final String   PATH          = "/space/4/security";

  private static final String   USER          = "mary";

  private static final String   TEST_PASSWORD = "testPassword";

  private static final String   NOT_ALLOWED   = "space.security.notAllowed";

  private static final String   INVALID_VALUE = "space.security.invalidValue";

  @Autowired
  private SecurityFilterChain   filterChain;

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private SpaceService          spaceService;

  @MockitoBean
  private SpaceSecurityService  spaceSecurityService;

  private MockMvc               mockMvc;

  private Space                 space;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
                             .addFilters(filterChain.getFilters().toArray(new Filter[0]))
                             .build();
    space = new Space();
    space.setId(SPACE_ID);
    when(spaceService.getSpaceById(SPACE_ID)).thenReturn(space);
  }

  @Test
  public void testGetUnknownSpace() throws Exception {
    when(spaceSecurityService.getSettings(SPACE_ID, USER)).thenThrow(new ObjectNotFoundException("gone"));
    mockMvc.perform(get(PATH).with(user())).andExpect(status().isNotFound());
  }

  @Test
  public void testGetSpaceNotVisible() throws Exception {
    when(spaceSecurityService.getSettings(SPACE_ID, USER)).thenThrow(new SpaceSecurityRefusedException(NOT_ALLOWED));
    mockMvc.perform(get(PATH).with(user())).andExpect(status().isForbidden()).andExpect(status().reason(NOT_ALLOWED));
  }

  @Test
  public void testGetAnswersTheStoredSettingsAndIsNeverStored() throws Exception {
    SpaceSecuritySettings settings = new SpaceSecuritySettings(true, false, true);
    when(spaceSecurityService.getSettings(SPACE_ID, USER)).thenReturn(settings);
    when(spaceSecurityService.isExternalMemberAllowed(settings)).thenReturn(true);
    when(spaceSecurityService.canManageSpace(space, USER)).thenReturn(true);

    mockMvc.perform(get(PATH).with(user()))
           .andExpect(status().isOk())
           .andExpect(header().string("Cache-Control", "no-store"))
           .andExpect(jsonPath("$.limitAccessManagement").value(true))
           .andExpect(jsonPath("$.keepDocumentsInside").value(false))
           .andExpect(jsonPath("$.externalMembersAllowed").value(true))
           .andExpect(jsonPath("$.canManageSettings").value(true))
           .andExpect(jsonPath("$.canManageExternals").value(false));
  }

  /**
   * The answer offers only what a request may change: a platform
   * administrator is not offered "External users can join this space", which
   * no request changes yet. Mutant: answering the right alone offers it.
   */
  @Test
  public void testGetOffersNoSettingARequestCannotChange() throws Exception {
    SpaceSecuritySettings settings = new SpaceSecuritySettings(true, false, true);
    when(spaceSecurityService.getSettings(SPACE_ID, USER)).thenReturn(settings);
    when(spaceSecurityService.canManageSpace(space, USER)).thenReturn(true);
    when(spaceService.isSuperManager(USER)).thenReturn(true);

    mockMvc.perform(get(PATH).with(user()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.canManageSettings").value(true))
           .andExpect(jsonPath("$.canManageExternals").value(false));
  }

  @Test
  public void testPatchChecksTheRightsThenPassesOnlyTheGivenSettings() throws Exception {
    SpaceSecuritySettings stored = new SpaceSecuritySettings(true, false, true);
    when(spaceSecurityService.updateSettings(SPACE_ID, new SpaceSecuritySettings(true, null, null), USER)).thenReturn(stored);

    mockMvc.perform(patch(PATH).with(user()).contentType(MediaType.APPLICATION_JSON).content("{\"limitAccessManagement\":true}"))
           .andExpect(status().isOk())
           .andExpect(header().string("Cache-Control", "no-store"))
           .andExpect(jsonPath("$.limitAccessManagement").value(true));
    InOrder inOrder = inOrder(spaceSecurityService);
    inOrder.verify(spaceSecurityService).checkCanUpdateSettings(SPACE_ID, new SpaceSecuritySettings(true, null, null), USER);
    inOrder.verify(spaceSecurityService).updateSettings(SPACE_ID, new SpaceSecuritySettings(true, null, null), USER);
  }

  /**
   * "Keep documents inside the space" alone: the rights are checked on it
   * whatever its value, and only it is passed on. Mutants: leaving it out of
   * the fields a request may change answers 400; leaving it unnamed in the
   * rights checks no right on it; dropping its value changes nothing.
   */
  @Test
  public void testPatchKeepDocumentsInsideChecksItsRightsThenPassesOnlyIt() throws Exception {
    for (boolean value : new boolean[] { true, false }) {
      clearInvocations(spaceSecurityService);
      SpaceSecuritySettings stored = new SpaceSecuritySettings(false, value, true);
      when(spaceSecurityService.updateSettings(eq(SPACE_ID), any(), eq(USER))).thenReturn(stored);

      mockMvc.perform(patch(PATH).with(user())
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content("{\"keepDocumentsInside\":" + value + "}"))
             .andExpect(status().isOk())
             .andExpect(header().string("Cache-Control", "no-store"))
             .andExpect(jsonPath("$.keepDocumentsInside").value(value))
             .andExpect(jsonPath("$.limitAccessManagement").value(false));
      InOrder inOrder = inOrder(spaceSecurityService);
      inOrder.verify(spaceSecurityService).checkCanUpdateSettings(SPACE_ID, new SpaceSecuritySettings(null, true, null), USER);
      inOrder.verify(spaceSecurityService).updateSettings(SPACE_ID, new SpaceSecuritySettings(null, value, null), USER);
    }
  }

  @Test
  public void testPatchBothDocumentsSettingsInOneRequest() throws Exception {
    SpaceSecuritySettings stored = new SpaceSecuritySettings(false, true, true);
    when(spaceSecurityService.updateSettings(eq(SPACE_ID), any(), eq(USER))).thenReturn(stored);

    mockMvc.perform(patch(PATH).with(user())
                               .contentType(MediaType.APPLICATION_JSON)
                               .content("{\"limitAccessManagement\":false,\"keepDocumentsInside\":true}"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.limitAccessManagement").value(false))
           .andExpect(jsonPath("$.keepDocumentsInside").value(true));
    InOrder inOrder = inOrder(spaceSecurityService);
    inOrder.verify(spaceSecurityService).checkCanUpdateSettings(SPACE_ID, new SpaceSecuritySettings(true, true, null), USER);
    inOrder.verify(spaceSecurityService).updateSettings(SPACE_ID, new SpaceSecuritySettings(false, true, null), USER);
  }

  @Test
  public void testPatchRefusesAnInvalidValueOfASettingTheUserMayChange() throws Exception {
    for (String body : new String[] { "{\"limitAccessManagement\":\"yes\"}", "{\"limitAccessManagement\":null}",
        "{\"keepDocumentsInside\":\"true\"}", "{\"keepDocumentsInside\":null}",
        "{\"limitAccessManagement\":true,\"keepDocumentsInside\":1}" }) {
      mockMvc.perform(patch(PATH).with(user()).contentType(MediaType.APPLICATION_JSON).content(body))
             .andExpect(status().isBadRequest())
             .andExpect(status().reason(INVALID_VALUE));
    }
    verify(spaceSecurityService, never()).updateSettings(anyLong(), any(), anyString());
  }

  @Test
  public void testPatchChecksTheRightsBeforeTheValue() throws Exception {
    doThrow(new SpaceSecurityRefusedException(NOT_ALLOWED)).when(spaceSecurityService)
                                                          .checkCanUpdateSettings(eq(SPACE_ID), any(), eq(USER));
    for (String body : new String[] { "{\"limitAccessManagement\":\"yes\"}", "{\"keepDocumentsInside\":\"yes\"}" }) {
      mockMvc.perform(patch(PATH).with(user()).contentType(MediaType.APPLICATION_JSON).content(body))
             .andExpect(status().isForbidden())
             .andExpect(status().reason(NOT_ALLOWED));
    }
    verify(spaceSecurityService, never()).updateSettings(anyLong(), any(), anyString());
  }

  @Test
  public void testPatchRefusesUnknownFieldsAndTheSettingNoRequestChangesYet() throws Exception {
    for (String body : new String[] { "{\"unknown\":true}", "{}", "{\"externalMembersAllowed\":false}",
        "{\"keepDocumentsInside\":true,\"externalMembersAllowed\":false}",
        "{\"limitAccessManagement\":true,\"unknown\":true}" }) {
      mockMvc.perform(patch(PATH).with(user()).contentType(MediaType.APPLICATION_JSON).content(body))
             .andExpect(status().isBadRequest())
             .andExpect(status().reason(INVALID_VALUE));
    }
    verify(spaceSecurityService, never()).checkCanUpdateSettings(anyLong(), any(), anyString());
    verify(spaceSecurityService, never()).updateSettings(anyLong(), any(), anyString());
  }

  @Test
  public void testPatchOfAnUnknownSpace() throws Exception {
    doThrow(new ObjectNotFoundException("gone")).when(spaceSecurityService).checkCanUpdateSettings(eq(SPACE_ID), any(), eq(USER));
    mockMvc.perform(patch(PATH).with(user()).contentType(MediaType.APPLICATION_JSON).content("{\"limitAccessManagement\":true}"))
           .andExpect(status().isNotFound());
  }

  @Test
  public void testPatchDuringAConcurrentChange() throws Exception {
    when(spaceSecurityService.updateSettings(eq(SPACE_ID),
                                             any(),
                                             eq(USER))).thenThrow(new IllegalStateException(new SpaceStorageException(SpaceStorageException.Type.CONCURRENT_UPDATE,
                                                                                                                      "locked")));
    mockMvc.perform(patch(PATH).with(user()).contentType(MediaType.APPLICATION_JSON).content("{\"limitAccessManagement\":true}"))
           .andExpect(status().isConflict())
           .andExpect(status().reason("space.concurrentUpdate"));
  }

  @Test
  public void testPatchNotPreparedAnswersItsCodeInTheBody() throws Exception {
    when(spaceSecurityService.updateSettings(eq(SPACE_ID),
                                             any(),
                                             eq(USER))).thenThrow(new SpaceSecurityChangeNotAppliedException(new IllegalStateException("internal")));
    mockMvc.perform(patch(PATH).with(user()).contentType(MediaType.APPLICATION_JSON).content("{\"limitAccessManagement\":true}"))
           .andExpect(status().isInternalServerError())
           .andExpect(header().string("Cache-Control", "no-store"))
           .andExpect(jsonPath("$.message").value("space.security.changeNotApplied"));
  }

  @Test
  public void testAnonymousIsRefused() throws Exception {
    mockMvc.perform(get(PATH)).andExpect(status().isForbidden());
    verify(spaceSecurityService, never()).getSettings(anyLong(), nullable(String.class));
  }

  private RequestPostProcessor user() {
    return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(USER)
                                                                                                  .password(TEST_PASSWORD)
                                                                                                  .authorities(new SimpleGrantedAuthority("users"));
  }

}
