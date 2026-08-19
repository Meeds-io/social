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
package io.meeds.social.core.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.portal.Constants;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.resources.ResourceBundleService;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BrandedEmailSenderTest {

  private static final String   USERNAME             = "john";

  private static final String   CONTENT_FRAGMENT_PATH = "assets/account-deactivation-confirmation-email-content.html";

  @Mock
  private OrganizationService   organizationService;

  @Mock
  private ResourceBundleService resourceBundleService;

  @Mock
  private BrandingService       brandingService;

  @Mock
  private MailService           mailService;

  @Mock
  private UserHandler           userHandler;

  @Mock
  private UserProfileHandler    userProfileHandler;

  @Mock
  private User                  user;

  @Mock
  private UserProfile           userProfile;

  @InjectMocks
  private BrandedEmailSender    brandedEmailSender;

  @Before
  @SneakyThrows
  public void setUp() {
    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(organizationService.getUserProfileHandler()).thenReturn(userProfileHandler);
    when(userHandler.findUserByName(USERNAME)).thenReturn(user);
    when(user.getEmail()).thenReturn("john@example.com");
    when(user.getDisplayName()).thenReturn("John Doe");
    when(userProfileHandler.findUserProfileByName(USERNAME)).thenReturn(userProfile);
    when(userProfile.getAttribute(Constants.USER_LANGUAGE)).thenReturn("en");
    when(resourceBundleService.getSharedString(anyString(), any())).thenReturn("TranslatedText");
    when(brandingService.getCompanyName()).thenReturn("MyCompany");
    when(brandingService.getThemeStyle()).thenReturn(Collections.singletonMap("primaryColor", "#123456"));
    when(mailService.getMailSession()).thenReturn(Session.getInstance(new Properties()));
    doNothing().when(mailService).sendMessage(any(MimeMessage.class));
  }

  @Test
  @SneakyThrows
  public void testSendEmailInjectsLayoutTokensAndContentFragment() {
    brandedEmailSender.sendEmail(USERNAME, "My Email Subject", CONTENT_FRAGMENT_PATH, Map.of());

    ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
    verify(mailService).sendMessage(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();
    assertEquals("My Email Subject", message.getSubject());
    String body = String.valueOf(message.getContent());
    assertTrue("Email title should be the subject", body.contains("My Email Subject"));
    assertTrue("User full name should be injected in the email body", body.contains("John Doe"));
    assertTrue("Company name should be injected in the email body", body.contains("MyCompany"));
    assertTrue("Primary color should be injected in the email body", body.contains("#123456"));
    assertFalse("All i18n placeholders should be resolved", body.contains("${"));
    assertFalse("The content fragment token should be replaced", body.contains("$EMAIL_CONTENT"));
  }

  @Test
  @SneakyThrows
  public void testSendEmailReplacesCustomTokens() {
    brandedEmailSender.sendEmail(USERNAME, "My Email Subject", CONTENT_FRAGMENT_PATH, Map.of("$SITE_NAME", "OverriddenName"));

    ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
    verify(mailService).sendMessage(messageCaptor.capture());
    String body = String.valueOf(messageCaptor.getValue().getContent());
    assertTrue("Custom tokens should be replaced before the standard ones", body.contains("OverriddenName"));
  }

  @Test
  @SneakyThrows
  public void testGetUserLangFallsBackToDefaultLanguage() {
    assertEquals("en", brandedEmailSender.getUserLang(USERNAME));

    when(userProfile.getAttribute(Constants.USER_LANGUAGE)).thenReturn("fr");
    assertEquals("fr", brandedEmailSender.getUserLang(USERNAME));

    when(userProfileHandler.findUserProfileByName(USERNAME)).thenReturn(null);
    assertEquals(ResourceBundleService.DEFAULT_CROWDIN_LANGUAGE, brandedEmailSender.getUserLang(USERNAME));
  }

}
