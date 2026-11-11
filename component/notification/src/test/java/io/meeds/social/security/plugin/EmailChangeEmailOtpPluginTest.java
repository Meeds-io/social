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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
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
public class EmailChangeEmailOtpPluginTest {

  private static final String       OTP_CODE  = "123456";

  private static final String       NEW_EMAIL = "john.new@example.org";

  private static final String       CACHE_KEY = "emailChange:john";

  private static final EmailChangeOtpPlugin.AddressCheck<String> ACCEPT = email -> null;

  @Mock
  private CacheService              cacheService;

  @Mock
  private SecureRandomService       secureRandomService;

  @Mock
  private OrganizationService       organizationService;

  @Mock
  private ResourceBundleService     resourceBundleService;

  @Mock
  private BrandingService           brandingService;

  @Mock
  private MailService               mailService;

  @Mock
  private SecureRandom              secureRandom;

  @Mock
  private ExoCache<String, String>  otpCache;

  @Mock
  private ExoCache<String, String>  otpSendLockCache;

  @Mock
  private UserHandler               userHandler;

  @Mock
  private UserProfileHandler        userProfileHandler;

  @Mock
  private User                      user;

  @Mock
  private UserProfile               userProfile;

  @Spy
  @InjectMocks
  private BrandedEmailSender        brandedEmailSender;

  @InjectMocks
  private EmailChangeEmailOtpPlugin plugin;

  @Before
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void setUp() throws Exception {
    plugin.setOtpTtl(5);
    plugin.setOtpLength(6);
    plugin.setSendInterval(60);
    plugin.setEmailChangeEmailBodyPath("assets/email-change-otp-email-content.html");
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
    assertEquals("emailChange", plugin.getName());
    assertEquals(EmailChangeOtpPlugin.NAME, plugin.getName());
    assertTrue(plugin.canUse("anyUser"));
  }

  /**
   * The code goes to the new address only, never to the address of the
   * account, and is stored bound to that address under this method's key
   */
  @Test
  public void testGenerateOtpCodeSendsToTheNewAddressOnly() throws Exception {
    assertNull(plugin.generateOtpCode("john", " " + NEW_EMAIL + " ", ACCEPT));

    verify(otpCache).put(CACHE_KEY, OTP_CODE + ":" + NEW_EMAIL);
    verify(otpSendLockCache).put(CACHE_KEY, "");
    verify(resourceBundleService).getSharedString(eq("social.emailChange.otp.email.subject"), any());
    verify(resourceBundleService).getSharedString(eq("social.emailChange.otp.email.label.requestReminder"), any());
    verify(resourceBundleService).getSharedString(eq("social.emailChange.otp.email.label.confirmMessage"), any());
    verify(resourceBundleService).getSharedString(eq("social.emailChange.otp.email.label.notRequested"), any());

    ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
    verify(mailService).sendMessage(messageCaptor.capture());
    MimeMessage message = messageCaptor.getValue();
    assertEquals(NEW_EMAIL, InternetAddress.toString(message.getAllRecipients()));
    String body = String.valueOf(message.getContent());
    assertTrue("OTP code should be injected in the email body", body.contains(OTP_CODE));
    assertTrue("User full name should be injected in the email body", body.contains("John Doe"));
    assertFalse("All i18n placeholders should be resolved", body.contains("${"));
  }

  /**
   * The generic send, which names no address, never mails a code of this
   * method, nor stores one
   */
  @Test
  public void testGenericSendIsRefused() throws Exception {
    assertThrows(IllegalArgumentException.class, () -> plugin.generateOtpCode("john"));
    verify(otpSendLockCache, never()).put(anyString(), anyString());
    assertThrows(IllegalArgumentException.class, () -> plugin.generateOtpCode("john", " ", ACCEPT));
    assertThrows(IllegalArgumentException.class, () -> plugin.generateOtpCode("john", null, ACCEPT));
    verify(otpCache, never()).put(anyString(), anyString());
    verify(mailService, never()).sendMessage(any(MimeMessage.class));
  }

  /**
   * The send lock is the user's, whatever the address: a user cannot mail
   * another address before the send interval is over, and the address is not
   * even checked meanwhile
   */
  @Test
  public void testSendLockHoldsWhateverTheAddress() throws Exception {
    when(otpSendLockCache.get(CACHE_KEY)).thenReturn("");
    AtomicInteger checks = new AtomicInteger();
    EmailChangeOtpPlugin.AddressCheck<String> countingCheck = email -> {
      checks.incrementAndGet();
      return null;
    };
    assertThrows(IllegalStateException.class, () -> plugin.generateOtpCode("john", NEW_EMAIL, countingCheck));
    assertThrows(IllegalStateException.class, () -> plugin.generateOtpCode("john", "another@example.org", countingCheck));
    assertEquals(0, checks.get());
    verify(otpCache, never()).put(anyString(), anyString());
    verify(mailService, never()).sendMessage(any(MimeMessage.class));
  }

  /**
   * The lock is taken before the address is checked, and kept when the check
   * refuses it: a second request within the send interval is refused as
   * throttled without checking its address, so the refusals of the check
   * are rate-limited like a send
   */
  @Test
  public void testRefusedAddressTakesTheSendLock() throws Exception {
    Map<String, String> sendLocks = new HashMap<>();
    when(otpSendLockCache.get(anyString())).thenAnswer(invocation -> sendLocks.get(invocation.getArgument(0)));
    doAnswer(invocation -> sendLocks.put(invocation.getArgument(0), invocation.getArgument(1))).when(otpSendLockCache)
                                                                                                 .put(anyString(), anyString());
    AtomicInteger checks = new AtomicInteger();
    String refusal = plugin.generateOtpCode("john", "used@example.org", email -> {
      assertTrue("The lock is taken before the address is checked", sendLocks.containsKey(CACHE_KEY));
      checks.incrementAndGet();
      return "ALREADY_EXISTS";
    });
    assertEquals("ALREADY_EXISTS", refusal);
    assertEquals(1, checks.get());
    assertTrue(sendLocks.containsKey(CACHE_KEY));

    assertThrows(IllegalStateException.class, () -> plugin.generateOtpCode("john", NEW_EMAIL, email -> {
      checks.incrementAndGet();
      return null;
    }));
    assertEquals("The address of a throttled request is not checked", 1, checks.get());
    verify(otpCache, never()).put(anyString(), anyString());
    verify(mailService, never()).sendMessage(any(MimeMessage.class));
  }

  /**
   * A mail that fails releases the lock and drops the code: no code was
   * received, so the user may ask again at once
   */
  @Test
  public void testAFailedSendReleasesTheSendLock() throws Exception {
    Map<String, String> sendLocks = new HashMap<>();
    when(otpSendLockCache.get(anyString())).thenAnswer(invocation -> sendLocks.get(invocation.getArgument(0)));
    doAnswer(invocation -> sendLocks.put(invocation.getArgument(0), invocation.getArgument(1))).when(otpSendLockCache)
                                                                                                 .put(anyString(), anyString());
    doAnswer(invocation -> sendLocks.remove(invocation.getArgument(0))).when(otpSendLockCache).remove(anyString());
    doThrow(new MessagingException("mail server down")).when(mailService).sendMessage(any(MimeMessage.class));

    assertThrows(Exception.class, () -> plugin.generateOtpCode("john", NEW_EMAIL, ACCEPT));
    assertFalse("the lock is released", sendLocks.containsKey(CACHE_KEY));
    verify(otpCache).remove(CACHE_KEY);

    doNothing().when(mailService).sendMessage(any(MimeMessage.class));
    assertNull("a new request is accepted at once", plugin.generateOtpCode("john", NEW_EMAIL, ACCEPT));
    assertTrue(sendLocks.containsKey(CACHE_KEY));
  }

  /**
   * A code validates bound to the address it was sent to only: neither alone
   * nor bound to another address. A wrong code keeps the send lock.
   */
  @Test
  public void testValidateOtpIsBoundToTheAddress() {
    when(otpCache.get(CACHE_KEY)).thenReturn(OTP_CODE + ":" + NEW_EMAIL);

    assertTrue(plugin.validateOtp("john", EmailChangeOtpPlugin.bindCode(OTP_CODE, NEW_EMAIL)));
    assertTrue(plugin.validateOtp("john", EmailChangeOtpPlugin.bindCode(OTP_CODE, "John.New@Example.org")));
    assertFalse(plugin.validateOtp("john", OTP_CODE));
    assertFalse(plugin.validateOtp("john", EmailChangeOtpPlugin.bindCode(OTP_CODE, "shared.mailbox@example.org")));
    assertFalse(plugin.validateOtp("john", EmailChangeOtpPlugin.bindCode("654321", NEW_EMAIL)));
    verify(otpSendLockCache, never()).remove(anyString());
  }

  @Test
  public void testValidateOtpIgnoresOtherPurposeCodes() {
    when(otpCache.get("email:john")).thenReturn(OTP_CODE + ":" + NEW_EMAIL);
    when(otpCache.get("accountDeactivationEmail:john")).thenReturn(OTP_CODE);
    assertFalse(plugin.validateOtp("john", OTP_CODE));
    assertFalse(plugin.validateOtp("john", EmailChangeOtpPlugin.bindCode(OTP_CODE, NEW_EMAIL)));
  }

  @Test
  public void testConsumeOtpRemovesTheBoundCodeOnce() {
    String boundCode = EmailChangeOtpPlugin.bindCode(OTP_CODE, NEW_EMAIL);
    when(otpCache.remove(CACHE_KEY)).thenReturn(boundCode, (String) null);
    assertTrue(plugin.consumeOtp("john", boundCode));
    assertFalse("A consumed code is gone for a concurrent request", plugin.consumeOtp("john", boundCode));
    verify(otpCache, times(2)).remove(CACHE_KEY);
  }

  /**
   * The bound value is unambiguous: a typed code holding anything but digits
   * binds to nothing, so no code and address pair can rebuild the value of
   * another address
   */
  @Test
  public void testBindCode() {
    assertEquals("123456:john.new@example.org", EmailChangeOtpPlugin.bindCode("123456", " John.New@Example.org "));
    assertNull(EmailChangeOtpPlugin.bindCode("123456:x", "john.new@example.org"));
    assertNull(EmailChangeOtpPlugin.bindCode("", "john.new@example.org"));
    assertNull(EmailChangeOtpPlugin.bindCode(null, "john.new@example.org"));
    assertNull(EmailChangeOtpPlugin.bindCode("123456", " "));
    assertNull(EmailChangeOtpPlugin.bindCode("123456", null));
    assertNotEquals(EmailChangeOtpPlugin.bindCode("123456", "a@example.org"),
                    EmailChangeOtpPlugin.bindCode("123456", "b@example.org"));
  }

  /**
   * The timing comes from the configuration of the email OTP, the template
   * from its own property
   */
  @Test
  public void testSettingsFollowTheEmailOtpConfiguration() {
    EmailChangeEmailOtpPlugin defaults = loadPlugin(Collections.emptyMap());
    assertEquals(60, defaults.getSendIntervalSeconds());
    assertEquals(5, defaults.getCodeTtlMinutes());
    assertEquals("assets/email-change-otp-email-content.html", defaults.getEmailBodyPath());

    EmailChangeEmailOtpPlugin configured = loadPlugin(Map.of("meeds.apiKey.otp.email.sendInterval", "90",
                                                             "meeds.apiKey.otp.email.ttl", "7",
                                                             "meeds.emailChange.otp.email.templatePath", "custom.html"));
    assertEquals(90, configured.getSendIntervalSeconds());
    assertEquals(7, configured.getCodeTtlMinutes());
    assertEquals("custom.html", configured.getEmailBodyPath());
  }

  private EmailChangeEmailOtpPlugin loadPlugin(Map<String, Object> properties) {
    try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
      context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
      context.registerBean(PropertySourcesPlaceholderConfigurer.class);
      // Ready-made singletons: the mocks themselves get no injection
      context.getBeanFactory().registerSingleton("cacheService", cacheService);
      context.getBeanFactory().registerSingleton("secureRandomService", secureRandomService);
      context.getBeanFactory().registerSingleton("resourceBundleService", resourceBundleService);
      context.getBeanFactory().registerSingleton("brandedEmailSender", mock(BrandedEmailSender.class));
      context.registerBean(EmailChangeEmailOtpPlugin.class);
      context.refresh();
      return context.getBean(EmailChangeEmailOtpPlugin.class);
    }
  }

}
