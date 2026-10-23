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
package io.meeds.social.organizationalchart.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

@SpringBootTest(classes = {
  OrganizationalChartSettingService.class,
})
@RunWith(SpringRunner.class)
public class OrganizationalChartSettingServiceTest {

  private static final String               ADMIN          = "admin";

  private static final String               USER           = "john";

  private static final String               APPLICATION_ID = "125";

  private static final Scope                SCOPE          = Scope.APPLICATION.id("organizationalChart125");

  @MockitoBean
  private SettingService                    settingService;

  @MockitoBean
  private IdentityManager                   identityManager;

  @MockitoBean
  private UserACL                           userAcl;

  @Autowired
  private OrganizationalChartSettingService organizationalChartSettingService;

  @Before
  public void setup() {
    org.exoplatform.services.security.Identity adminAclIdentity = mock(org.exoplatform.services.security.Identity.class);
    org.exoplatform.services.security.Identity userAclIdentity = mock(org.exoplatform.services.security.Identity.class);
    when(userAcl.getUserIdentity(ADMIN)).thenReturn(adminAclIdentity);
    when(userAcl.getUserIdentity(USER)).thenReturn(userAclIdentity);
    when(userAcl.isAdministrator(adminAclIdentity)).thenReturn(true);
  }

  @Test
  public void canEditOnlyForAdministrators() {
    assertTrue(organizationalChartSettingService.canEdit(ADMIN));
    assertFalse(organizationalChartSettingService.canEdit(USER));
    assertFalse(organizationalChartSettingService.canEdit(null));
    assertFalse(organizationalChartSettingService.canEdit(" "));
  }

  @Test
  public void saveSettingsRefusesANonAdministrator() {
    assertThrows(IllegalAccessException.class,
                 () -> organizationalChartSettingService.saveSettings(APPLICATION_ID, "@connected@", true, USER));
    verify(settingService, never()).set(any(), any(), anyString(), any());
  }

  @Test
  public void saveSettingsStoresTheConnectedUserMarker() throws IllegalAccessException {
    organizationalChartSettingService.saveSettings(APPLICATION_ID, "@connected@", false, ADMIN);
    verify(settingService).set(eq(Context.GLOBAL),
                               eq(SCOPE),
                               eq("organizationalChartCenterUser"),
                               argThat(value -> "@connected@".equals(value.getValue())));
    verify(settingService).set(eq(Context.GLOBAL),
                               eq(SCOPE),
                               eq("organizationalChartHasHeaderTitle"),
                               argThat(value -> "false".equals(value.getValue())));
  }

  @Test
  public void saveSettingsStoresAnEnabledUserIdentity() throws IllegalAccessException {
    mockIdentity("12", OrganizationIdentityProvider.NAME, true, false);
    organizationalChartSettingService.saveSettings(APPLICATION_ID, "12", true, ADMIN);
    verify(settingService).set(eq(Context.GLOBAL),
                               eq(SCOPE),
                               eq("organizationalChartCenterUser"),
                               argThat(value -> "12".equals(value.getValue())));
    verify(settingService).set(eq(Context.GLOBAL),
                               eq(SCOPE),
                               eq("organizationalChartHasHeaderTitle"),
                               argThat(value -> "true".equals(value.getValue())));
  }

  @Test
  public void saveSettingsWithoutCenterUserKeepsTheSavedOne() throws IllegalAccessException {
    organizationalChartSettingService.saveSettings(APPLICATION_ID, null, true, ADMIN);
    verify(settingService, never()).set(any(), any(), eq("organizationalChartCenterUser"), any());
    verify(settingService).set(eq(Context.GLOBAL), eq(SCOPE), eq("organizationalChartHasHeaderTitle"), any());
  }

  @Test
  public void saveSettingsRefusesAnInvalidApplicationId() {
    assertInvalid(null, "@connected@", OrganizationalChartSettingService.INVALID_APPLICATION_MESSAGE);
    assertInvalid("", "@connected@", OrganizationalChartSettingService.INVALID_APPLICATION_MESSAGE);
    assertInvalid("organizationalChart125", "@connected@", OrganizationalChartSettingService.INVALID_APPLICATION_MESSAGE);
    assertInvalid("12/../3", "@connected@", OrganizationalChartSettingService.INVALID_APPLICATION_MESSAGE);
    assertInvalid("1".repeat(19), "@connected@", OrganizationalChartSettingService.INVALID_APPLICATION_MESSAGE);
    verify(settingService, never()).set(any(), any(), anyString(), any());
  }

  @Test
  public void saveSettingsRefusesAnInvalidCenterUser() {
    mockIdentity("20", SpaceIdentityProvider.NAME, true, false);
    mockIdentity("21", OrganizationIdentityProvider.NAME, false, false);
    mockIdentity("22", OrganizationIdentityProvider.NAME, true, true);
    assertInvalid(APPLICATION_ID, "", OrganizationalChartSettingService.INVALID_CENTER_USER_MESSAGE);
    assertInvalid(APPLICATION_ID, "john", OrganizationalChartSettingService.INVALID_CENTER_USER_MESSAGE);
    assertInvalid(APPLICATION_ID, "\"};alert(1);//", OrganizationalChartSettingService.INVALID_CENTER_USER_MESSAGE);
    assertInvalid(APPLICATION_ID, "19", OrganizationalChartSettingService.INVALID_CENTER_USER_MESSAGE);
    assertInvalid(APPLICATION_ID, "20", OrganizationalChartSettingService.INVALID_CENTER_USER_MESSAGE);
    assertInvalid(APPLICATION_ID, "21", OrganizationalChartSettingService.INVALID_CENTER_USER_MESSAGE);
    assertInvalid(APPLICATION_ID, "22", OrganizationalChartSettingService.INVALID_CENTER_USER_MESSAGE);
    verify(settingService, never()).set(any(), any(), anyString(), any());
  }

  @Test
  public void saveSettingsKeepsAStoredCenterUserDisabledSince() throws IllegalAccessException {
    mockIdentity("21", OrganizationIdentityProvider.NAME, false, false);
    when(settingService.get(Context.GLOBAL, SCOPE, "organizationalChartCenterUser")).thenAnswer(invocation -> SettingValue.create("21"));
    organizationalChartSettingService.saveSettings(APPLICATION_ID, "21", false, ADMIN);
    verify(settingService).set(eq(Context.GLOBAL),
                               eq(SCOPE),
                               eq("organizationalChartCenterUser"),
                               argThat(value -> "21".equals(value.getValue())));

    // Stored for another application, it is still refused
    assertInvalid("126", "21", OrganizationalChartSettingService.INVALID_CENTER_USER_MESSAGE);
  }

  private void mockIdentity(String id, String providerId, boolean enabled, boolean deleted) {
    Identity identity = new Identity(id);
    identity.setProviderId(providerId);
    identity.setEnable(enabled);
    identity.setDeleted(deleted);
    when(identityManager.getIdentity(id)).thenReturn(identity);
  }

  private void assertInvalid(String applicationId, String centerUser, String message) {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                      () -> organizationalChartSettingService.saveSettings(applicationId,
                                                                                                           centerUser,
                                                                                                           true,
                                                                                                           ADMIN));
    assertEquals(message, exception.getMessage());
  }

}
