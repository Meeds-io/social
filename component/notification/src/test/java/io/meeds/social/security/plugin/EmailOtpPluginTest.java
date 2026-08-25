/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
package io.meeds.social.security.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Properties;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.portal.Constants;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.web.security.security.SecureRandomService;

@RunWith(MockitoJUnitRunner.Silent.class)
public class EmailOtpPluginTest {

  private static final String      OTP_CODE = "123456";

  @Mock
  private CacheService             cacheService;

  @Mock
  private SecureRandomService      secureRandomService;

  @Mock
  private OrganizationService      organizationService;

  @Mock
  private ResourceBundleService    resourceBundleService;

  @Mock
  private BrandingService          brandingService;

  @Mock
  private MailService              mailService;

  @Mock
  private SecureRandom             secureRandom;

  @Mock
  private ExoCache<String, String> otpCache;

  @Mock
  private UserHandler              userHandler;

  @Mock
  private UserProfileHandler       userProfileHandler;

  @Mock
  private User                     user;

  @Mock
  private UserProfile              userProfile;

  @InjectMocks
  private EmailOtpPlugin           plugin;

  @Before
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void setUp() throws Exception {
    plugin.setOtpTtl(5);
    plugin.setOtpLength(6);
    plugin.setEmailBodyPath("fake-path.html");
    plugin.setEmailBodyTemplate("Hello $USER_FULL_NAME, code: ####");
    when(cacheService.getCacheInstance("otp.email")).thenReturn((ExoCache) otpCache); // NOSONAR
    when(secureRandomService.getSecureRandom()).thenReturn(secureRandom);
    when(secureRandom.nextLong(anyLong(), anyLong())).thenReturn(123456L);

    when(organizationService.getUserHandler()).thenReturn(userHandler);
    when(organizationService.getUserProfileHandler()).thenReturn(userProfileHandler);

    when(userHandler.findUserByName("john")).thenReturn(user);
    when(user.getEmail()).thenReturn("john@example.com");
    when(user.getDisplayName()).thenReturn("John Doe");

    when(userProfileHandler.findUserProfileByName("john")).thenReturn(userProfile);
    when(userProfile.getAttribute(Constants.USER_LANGUAGE)).thenReturn("en");

    when(resourceBundleService.getSharedString(anyString(), any())).thenReturn("TranslatedText");
    when(brandingService.getCompanyName()).thenReturn("MyCompany");
    when(brandingService.getThemeStyle()).thenReturn(Collections.singletonMap("primaryColor", "#123456"));

    Session mockSession = Session.getInstance(new Properties());
    when(mailService.getMailSession()).thenReturn(mockSession);

    doNothing().when(mailService).sendMessage(any(MimeMessage.class));
  }

  @Test
  public void testGetNameAndCanUse() {
    assertEquals("email", plugin.getName());
    assertTrue(plugin.canUse("anyUser"));
  }

  @Test
  public void testValidateOtpValid() {
    when(otpCache.get("john")).thenReturn(OTP_CODE);
    assertTrue(plugin.validateOtp("john", OTP_CODE));
  }

  @Test
  public void testValidateOtpInvalid() {
    when(otpCache.get("john")).thenReturn("999999");
    assertFalse(plugin.validateOtp("john", OTP_CODE));
  }

  @Test
  public void testGenerateOtpCodeSuccess() throws Exception {
    plugin.generateOtpCode("john");

    verify(otpCache).put(eq("john"), eq(OTP_CODE));
    verify(mailService).sendMessage(any(MimeMessage.class));
  }

}
