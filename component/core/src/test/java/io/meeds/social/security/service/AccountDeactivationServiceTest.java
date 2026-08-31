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
import static io.meeds.social.security.service.AccountDeactivationService.ACCOUNT_DELETION_REQUESTED_EVENT;
import static io.meeds.social.security.service.AccountDeactivationService.DELETION_REQUEST_SCOPE;
import static io.meeds.social.security.service.AccountDeactivationService.DELETION_REQUEST_SETTING_NAME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;

import io.meeds.portal.security.model.RegistrationSetting;
import io.meeds.portal.security.service.SecuritySettingService;
import io.meeds.social.core.mail.BrandedEmailSender;
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

  @Mock
  private SettingService             settingService;

  @Mock
  private BrandedEmailSender         brandedEmailSender;

  @Mock
  private BrandingService            brandingService;

  @Mock
  private ResourceBundleService      resourceBundleService;

  @InjectMocks
  private AccountDeactivationService accountDeactivationService;

  private RegistrationSetting        registrationSetting;

  @Before
  @SneakyThrows
  public void setUp() {
    registrationSetting = new RegistrationSetting();
    // the service reads the admin flags through the direct (cluster-safe)
    // readers, not through the memoized RegistrationSetting object
    when(securitySettingService.isAccountDeactivationEnabled()).thenAnswer(invocation -> registrationSetting.isAccountDeactivationEnabled());
    when(securitySettingService.isAccountDeletionEnabled()).thenAnswer(invocation -> registrationSetting.isAccountDeletionEnabled());
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(userHandler.findUserByName(USERNAME)).thenReturn(user);
    when(identityManager.getOrCreateUserIdentity(USERNAME)).thenReturn(identity);
    when(identity.getId()).thenReturn(IDENTITY_ID);
    when(brandedEmailSender.getUserLang(USERNAME)).thenReturn("en");
    when(brandingService.getCompanyName()).thenReturn("MyCompany");
    when(resourceBundleService.getSharedString(anyString(), any())).thenReturn("TranslatedText");
    accountDeactivationService.setEmailBodyPath("assets/account-deactivation-confirmation-email-content.html");
    accountDeactivationService.setDeletionEmailBodyPath("assets/account-deletion-confirmation-email-content.html");
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
  public void testDeactivationAllowedForUiCreatedUser() {
    registrationSetting.setAccountDeactivationEnabled(true);
    when(user.isInternalStore()).thenReturn(true);
    when(user.getCreationSource()).thenReturn(AccountDeactivationService.UI_CREATION_SOURCE);
    assertTrue(accountDeactivationService.isDeactivationAllowed(USERNAME));
  }

  @Test
  public void testDeactivationAllowedWhenCreationSourceIsUnknown() {
    // accounts created before the creationSource stamping keep the option
    registrationSetting.setAccountDeactivationEnabled(true);
    when(user.isInternalStore()).thenReturn(true);
    when(user.getCreationSource()).thenReturn(null);
    assertTrue(accountDeactivationService.isDeactivationAllowed(USERNAME));
  }

  @Test
  public void testDeactivationNotAllowedForUserCreatedByAnotherSource() {
    // any known source other than the UI one, e.g. the CSV file importation
    registrationSetting.setAccountDeactivationEnabled(true);
    when(user.isInternalStore()).thenReturn(true);
    when(user.getCreationSource()).thenReturn("fileImportation");
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
  public void testRequestDeactivationWithDeletionRejectedWhenDeletionOptionOff() {
    allowDeactivation();
    assertThrows(IllegalStateException.class,
                 () -> accountDeactivationService.requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, true));

    verify(otpService, never()).validateOtp(anyString(), anyString(), anyString());
    verify(settingService, never()).set(any(), any(), anyString(), any());
    verify(userHandler, never()).setEnabled(anyString(), anyBoolean(), anyBoolean());
    verify(listenerService, never()).broadcast(anyString(), anyString(), anyString());
    verify(identityRegistry, never()).unregister(anyString());
  }

  @Test
  @SneakyThrows
  public void testRequestDeactivationWithDeletionRecordsRequestAndBroadcasts() {
    allowDeletion();
    long beforeRequestTime = System.currentTimeMillis();
    accountDeactivationService.requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, true);

    verify(settingService).set(eq(Context.USER.id(USERNAME)),
                               eq(DELETION_REQUEST_SCOPE),
                               eq(DELETION_REQUEST_SETTING_NAME),
                               argThat(value -> Long.parseLong(value.getValue().toString()) >= beforeRequestTime));
    verify(userHandler).setEnabled(USERNAME, false, true);
    verify(listenerService).broadcast(ACCOUNT_DEACTIVATION_REQUESTED_EVENT, USERNAME, IDENTITY_ID);
    verify(listenerService).broadcast(ACCOUNT_DELETION_REQUESTED_EVENT, USERNAME, IDENTITY_ID);
    verify(identityRegistry).unregister(USERNAME);
    verify(conversationRegistry).unregisterByUserId(USERNAME);
  }

  @Test
  @SneakyThrows
  public void testRequestDeactivationWithDeletionRecordsRequestBeforeDisabling() {
    allowDeletion();
    accountDeactivationService.requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, true);

    InOrder order = inOrder(settingService, userHandler);
    order.verify(settingService).set(eq(Context.USER.id(USERNAME)),
                                     eq(DELETION_REQUEST_SCOPE),
                                     eq(DELETION_REQUEST_SETTING_NAME),
                                     any());
    order.verify(userHandler).setEnabled(USERNAME, false, true);
  }

  @Test
  @SneakyThrows
  public void testRequestDeactivationWithoutDeletionClearsMarkerAndSkipsDeletionEvent() {
    allowDeletion();
    accountDeactivationService.requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, false);

    verify(settingService, never()).set(any(), any(), anyString(), any());
    verify(settingService).remove(Context.USER.id(USERNAME), DELETION_REQUEST_SCOPE, DELETION_REQUEST_SETTING_NAME);
    verify(listenerService).broadcast(ACCOUNT_DEACTIVATION_REQUESTED_EVENT, USERNAME, IDENTITY_ID);
    verify(listenerService, never()).broadcast(ACCOUNT_DELETION_REQUESTED_EVENT, USERNAME, IDENTITY_ID);
    verify(userHandler).setEnabled(USERNAME, false, true);
  }

  @Test
  @SneakyThrows
  public void testRequestDeactivationWithDeletionAbortsWhenRecordingFails() {
    allowDeletion();
    doThrow(new RuntimeException("Settings store down")).when(settingService).set(any(), any(), anyString(), any());
    assertThrows(RuntimeException.class,
                 () -> accountDeactivationService.requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, true));

    verify(brandedEmailSender, never()).sendEmail(anyString(), anyString(), anyString(), anyMap());
    verify(userHandler, never()).setEnabled(anyString(), anyBoolean(), anyBoolean());
    verify(listenerService, never()).broadcast(anyString(), anyString(), anyString());
    verify(identityRegistry, never()).unregister(anyString());
    verify(conversationRegistry, never()).unregisterByUserId(anyString());
  }

  @Test
  public void testDeletionAllowedWhenBothOptionsOnForInternalUser() {
    allowDeletion();
    assertTrue(accountDeactivationService.isDeletionAllowed(USERNAME));
  }

  @Test
  public void testDeletionNotAllowedWhenDeletionOptionOff() {
    allowDeactivation();
    assertFalse(accountDeactivationService.isDeletionAllowed(USERNAME));
  }

  @Test
  public void testDeletionNotAllowedWhenDeactivationOptionOff() {
    registrationSetting.setAccountDeactivationEnabled(false);
    registrationSetting.setAccountDeletionEnabled(true);
    when(user.isInternalStore()).thenReturn(true);
    assertFalse(accountDeactivationService.isDeletionAllowed(USERNAME));
  }

  @Test
  public void testDeletionNotAllowedForExternalStoreUser() {
    allowDeletion();
    when(user.isInternalStore()).thenReturn(false);
    assertFalse(accountDeactivationService.isDeletionAllowed(USERNAME));
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

  @Test
  @SneakyThrows
  public void testRequestDeactivationSendsConfirmationEmailBeforeDisabling() {
    allowDeactivation();
    when(resourceBundleService.getSharedString(eq("social.accountDeactivation.confirmation.email.subject"),
                                               any())).thenReturn("Your access to {0}");
    accountDeactivationService.requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, false);

    ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
    InOrder order = inOrder(brandedEmailSender, userHandler);
    order.verify(brandedEmailSender).sendEmail(eq(USERNAME),
                                               subjectCaptor.capture(),
                                               eq("assets/account-deactivation-confirmation-email-content.html"),
                                               anyMap());
    order.verify(userHandler).setEnabled(USERNAME, false, true);

    assertTrue("Email subject should carry the platform name",
               subjectCaptor.getValue().contains("MyCompany"));
  }

  @Test
  @SneakyThrows
  public void testRequestDeactivationWithDeletionSendsDeletionConfirmationEmail() {
    allowDeletion();
    accountDeactivationService.requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, true);

    verify(brandedEmailSender).sendEmail(eq(USERNAME),
                                         anyString(),
                                         eq("assets/account-deletion-confirmation-email-content.html"),
                                         anyMap());
  }

  @Test
  @SneakyThrows
  public void testRequestDeactivationCompletesWhenEmailSendingFails() {
    allowDeactivation();
    doThrow(new RuntimeException("SMTP down")).when(brandedEmailSender)
                                              .sendEmail(anyString(), anyString(), anyString(), anyMap());
    accountDeactivationService.requestDeactivation(USERNAME, OTP_METHOD, OTP_CODE, false);

    verify(userHandler).setEnabled(USERNAME, false, true);
    verify(listenerService).broadcast(ACCOUNT_DEACTIVATION_REQUESTED_EVENT, USERNAME, IDENTITY_ID);
    verify(identityRegistry).unregister(USERNAME);
    verify(conversationRegistry).unregisterByUserId(USERNAME);
  }

  private void allowDeactivation() {
    registrationSetting.setAccountDeactivationEnabled(true);
    when(user.isInternalStore()).thenReturn(true);
  }

  private void allowDeletion() {
    allowDeactivation();
    registrationSetting.setAccountDeletionEnabled(true);
  }

}
