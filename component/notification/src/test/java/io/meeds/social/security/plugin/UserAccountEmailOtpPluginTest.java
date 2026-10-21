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
package io.meeds.social.security.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.SecureRandom;
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
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;

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

import io.meeds.social.core.mail.BrandedEmailSender;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UserAccountEmailOtpPluginTest {

  private static final String               OTP_CODE = "123456";

  @Mock
  private CacheService                      cacheService;

  @Mock
  private SecureRandomService               secureRandomService;

  @Mock
  private OrganizationService               organizationService;

  @Mock
  private ResourceBundleService             resourceBundleService;

  @Mock
  private BrandingService                   brandingService;

  @Mock
  private MailService                       mailService;

  @Mock
  private SecureRandom                      secureRandom;

  @Mock
  private ExoCache<String, String>          otpCache;

  @Mock
  private ExoCache<String, String>          otpSendLockCache;

  @Mock
  private UserHandler                       userHandler;

  @Mock
  private UserProfileHandler                userProfileHandler;

  @Mock
  private User                              user;

  @Mock
  private UserProfile                       userProfile;

  @Spy
  @InjectMocks
  private BrandedEmailSender                brandedEmailSender;

  @InjectMocks
  private UserAccountEmailOtpPlugin plugin;

  @Before
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void setUp() throws Exception {
    plugin.setOtpTtl(5);
    plugin.setOtpLength(6);
    plugin.setSendInterval(60);
    plugin.setUserAccountEmailBodyPath("assets/user-account-otp-email-content.html");
    plugin.setBrandedEmailSender(brandedEmailSender);
    when(cacheService.getCacheInstance("otp.email")).thenReturn((ExoCache) otpCache); // NOSONAR
    when(cacheService.getCacheInstance("otp.email.sendLock")).thenReturn((ExoCache) otpSendLockCache); // NOSONAR
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
    assertEquals("userAccountEmail", plugin.getName());
    assertTrue(plugin.canUse("anyUser"));
  }

  @Test
  public void testValidateOtpUsesPurposeBoundCacheKey() {
    when(otpCache.get("userAccountEmail:john")).thenReturn(OTP_CODE);
    assertTrue(plugin.validateOtp("john", OTP_CODE));
    assertFalse(plugin.validateOtp("john", "999999"));
  }

  @Test
  public void testValidateOtpIgnoresOtherPurposeCodes() {
    when(otpCache.get("email:john")).thenReturn(OTP_CODE);
    when(otpCache.get("accountDeactivationEmail:john")).thenReturn(OTP_CODE);
    when(otpCache.get("john")).thenReturn(OTP_CODE);
    assertFalse("A code generated for another purpose must not confirm an administrator session",
                plugin.validateOtp("john", OTP_CODE));
  }

  @Test
  public void testConsumeOtpRemovesTheCodeOnce() {
    when(otpCache.remove("userAccountEmail:john")).thenReturn(OTP_CODE, (String) null);
    assertTrue(plugin.consumeOtp("john", OTP_CODE));
    assertFalse("A consumed code is gone for a concurrent request", plugin.consumeOtp("john", OTP_CODE));
    verify(otpCache, org.mockito.Mockito.times(2)).remove("userAccountEmail:john");
  }

  @Test
  public void testConsumeOtpRefusesAnotherCode() {
    when(otpCache.remove("userAccountEmail:john")).thenReturn("999999");
    assertFalse(plugin.consumeOtp("john", OTP_CODE));
  }

  @Test
  public void testGenerateOtpCodeUsesDedicatedSubjectAndTemplate() throws Exception {
    plugin.generateOtpCode("john");

    verify(otpCache).put("userAccountEmail:john", OTP_CODE);
    verify(resourceBundleService).getSharedString(eq("social.userAccount.otp.email.subject"), any());
    verify(resourceBundleService).getSharedString(eq("otp.email.label.requestAccessReminder"), any());
    verify(resourceBundleService).getSharedString(eq("otp.email.label.requestAccessConfirmMessage"), any());
    verify(resourceBundleService).getSharedString(eq("otp.email.label.greetings"), any());

    ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
    verify(mailService).sendMessage(messageCaptor.capture());
    String body = String.valueOf(messageCaptor.getValue().getContent());
    assertTrue("OTP code should be injected in the email body", body.contains(OTP_CODE));
    assertFalse("All i18n placeholders should be resolved", body.contains("${"));
  }

  /**
   * The timing the confirmation dialog shows comes from the configuration of
   * the email OTP: the send interval in seconds, the validity in minutes
   */
  @Test
  public void testTimingFollowsTheEmailOtpConfiguration() {
    UserAccountEmailOtpPlugin defaults = loadPlugin(Collections.emptyMap());
    assertEquals(60, defaults.getSendIntervalSeconds());
    assertEquals(5, defaults.getCodeTtlMinutes());
    assertEquals("assets/user-account-otp-email-content.html", defaults.getEmailBodyPath());

    UserAccountEmailOtpPlugin configured = loadPlugin(Map.of("meeds.apiKey.otp.email.sendInterval", "90",
                                                             "meeds.apiKey.otp.email.ttl", "7",
                                                             "meeds.userAccount.otp.email.templatePath", "custom.html"));
    assertEquals(90, configured.getSendIntervalSeconds());
    assertEquals(7, configured.getCodeTtlMinutes());
    assertEquals("custom.html", configured.getEmailBodyPath());
  }

  private UserAccountEmailOtpPlugin loadPlugin(Map<String, Object> properties) {
    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
      context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
      context.registerBean(PropertySourcesPlaceholderConfigurer.class);
      // Ready-made singletons: the mocks themselves get no injection
      context.getBeanFactory().registerSingleton("cacheService", cacheService);
      context.getBeanFactory().registerSingleton("secureRandomService", secureRandomService);
      context.getBeanFactory().registerSingleton("resourceBundleService", resourceBundleService);
      context.getBeanFactory().registerSingleton("brandedEmailSender", mock(BrandedEmailSender.class));
      context.registerBean(UserAccountEmailOtpPlugin.class);
      context.refresh();
      return context.getBean(UserAccountEmailOtpPlugin.class);
    }
  }

}
