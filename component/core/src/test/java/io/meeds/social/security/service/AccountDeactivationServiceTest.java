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
package io.meeds.social.security.service;

import static io.meeds.social.security.service.AccountDeactivationService.ACCOUNT_DEACTIVATION_REQUESTED_EVENT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.portal.security.model.RegistrationSetting;
import io.meeds.portal.security.service.SecuritySettingService;
import io.meeds.web.security.service.OtpService;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AccountDeactivationServiceTest {

  private static final String        USERNAME    = "john";

  private static final String        IDENTITY_ID = "123";

  private static final String        OTP_METHOD  = "accountDeactivationEmail";

  private static final String        OTP_CODE    = "12345";

  @Mock
  private SecuritySettingService     securitySettingService;

  @Mock
  private OrganizationService        organizationService;

  @Mock
  private UserHandler                userHandler;

  @Mock
  private User                       user;

  @Mock
  private IdentityManager            identityManager;

  @Mock
  private Identity                   identity;

  @Mock
  private ListenerService            listenerService;

  @Mock
  private IdentityRegistry           identityRegistry;

  @Mock
  private ConversationRegistry       conversationRegistry;

  @Mock
  private OtpService                 otpService;

  @InjectMocks
  private AccountDeactivationService accountDeactivationService;

  private RegistrationSetting        registrationSetting;

  @Before
  @SneakyThrows
  public void setUp() {
    registrationSetting = new RegistrationSetting();
    when(securitySettingService.getRegistrationSetting()).thenReturn(registrationSetting);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(userHandler.findUserByName(USERNAME)).thenReturn(user);
    when(identityManager.getOrCreateUserIdentity(USERNAME)).thenReturn(identity);
    when(identity.getId()).thenReturn(IDENTITY_ID);
  }

  @Test
  public void testDeactivationNotAllowedWhenAdminOptionOff() {
    registrationSetting.setAccountDeactivationEnabled(false);
    when(user.isInternalStore()).thenReturn(true);
    assertFalse(accountDeactivationService.isDeactivationAllowed(USERNAME));
  }

  @Test
  public void testDeactivationAllowedForInternalUserWhenAdminOptionOn() {
    registrationSetting.setAccountDeactivationEnabled(true);
    when(user.isInternalStore()).thenReturn(true);
    assertTrue(accountDeactivationService.isDeactivationAllowed(USERNAME));
  }

  @Test
  public void testDeactivationNotAllowedForExternalStoreUser() {
    registrationSetting.setAccountDeactivationEnabled(true);
    when(user.isInternalStore()).thenReturn(false);
    assertFalse(accountDeactivationService.isDeactivationAllowed(USERNAME));
  }

  @Test
  @SneakyThrows
  public void testDeactivationNotAllowedForUnknownUser() {
    registrationSetting.setAccountDeactivationEnabled(true);
    when(userHandler.findUserByName(USERNAME)).thenReturn(null);
    assertFalse(accountDeactivationService.isDeactivationAllowed(USERNAME));
  }

  @Test
  @SneakyThrows
  public void testRequestDeactivationDisablesUserAndInvalidatesSessions() {
    allowDeactivation();
    accountDeactivationService.requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, false);

    verify(otpService).validateOtp(USERNAME, OTP_METHOD, OTP_CODE);
    verify(userHandler).setEnabled(USERNAME, false, true);
    verify(listenerService).broadcast(ACCOUNT_DEACTIVATION_REQUESTED_EVENT, USERNAME, IDENTITY_ID);
    verify(identityRegistry).unregister(USERNAME);
    verify(conversationRegistry).unregisterByUserId(USERNAME);
  }

  @Test
  @SneakyThrows
  public void testRequestDeactivationWithDeletionRefusedUntilImplemented() {
    allowDeactivation();
    assertThrows(UnsupportedOperationException.class,
                 () -> accountDeactivationService.requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, true));

    verify(otpService, never()).validateOtp(anyString(), anyString(), anyString());
    verify(userHandler, never()).setEnabled(anyString(), anyBoolean(), anyBoolean());
    verify(listenerService, never()).broadcast(anyString(), anyString(), anyString());
    verify(identityRegistry, never()).unregister(anyString());
  }

  @Test
  @SneakyThrows
  public void testRequestDeactivationRejectedWhenNotAllowed() {
    registrationSetting.setAccountDeactivationEnabled(false);
    assertThrows(IllegalStateException.class,
                 () -> accountDeactivationService.requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, false));

    verify(otpService, never()).validateOtp(anyString(), anyString(), anyString());
    verify(userHandler, never()).setEnabled(anyString(), anyBoolean(), anyBoolean());
  }

  @Test
  @SneakyThrows
  public void testRequestDeactivationRejectedWhenOtpCodeInvalid() {
    allowDeactivation();
    doThrow(new IllegalAccessException()).when(otpService).validateOtp(USERNAME, OTP_METHOD, OTP_CODE);
    assertThrows(IllegalAccessException.class,
                 () -> accountDeactivationService.requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, false));

    verify(userHandler, never()).setEnabled(anyString(), anyBoolean(), anyBoolean());
    verify(identityRegistry, never()).unregister(anyString());
  }

  private void allowDeactivation() {
    registrationSetting.setAccountDeactivationEnabled(true);
    when(user.isInternalStore()).thenReturn(true);
  }

}
