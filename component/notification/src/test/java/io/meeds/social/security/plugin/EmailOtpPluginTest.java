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

import static org.mockito.ArgumentMatchers.endsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
  private ExoCache<String, String> otpSendLockCache;

  @Mock
  private UserHandler              userHandler;

  @Mock
  private UserProfileHandler       userProfileHandler;

  @Mock
  private User                     user;

  @Mock
  private UserProfile              userProfile;

  @Spy
  @InjectMocks
  private BrandedEmailSender       brandedEmailSender;

  @InjectMocks
  private EmailOtpPlugin           plugin;

  @Before
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void setUp() throws Exception {
    plugin.setOtpTtl(5);
    plugin.setOtpLength(6);
    plugin.setSendInterval(60);
    plugin.setEmailBodyPath("assets/otp-email-content.html");
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
  public void testConsumeOtpRemovesTheCodeOnce() {
    Map<String, String> codes = storeCodesIn(new HashMap<>());
    codes.put("email:john", liveCode(OTP_CODE));
    assertTrue(plugin.consumeOtp("john", OTP_CODE));
    assertFalse(plugin.consumeOtp("john", OTP_CODE));
    assertFalse(codes.containsKey("email:john"));
  }

  @Test
  public void testConsumeOtpRefusesAnotherCodeAndKeepsIt() {
    Map<String, String> codes = storeCodesIn(new HashMap<>());
    codes.put("email:john", liveCode("999999"));
    assertFalse(plugin.consumeOtp("john", OTP_CODE));
    assertTrue("A code that was not consumed must stay valid", plugin.validateOtp("john", "999999"));
  }

  @Test
  public void testACodeSentBetweenTheReadAndTheRemovalIsPutBack() {
    String newerCode = liveCode("999999");
    when(otpCache.get("email:john")).thenReturn(liveCode(OTP_CODE));
    when(otpCache.remove("email:john")).thenReturn(newerCode);
    assertFalse(plugin.consumeOtp("john", OTP_CODE));
    verify(otpCache).put("email:john", newerCode);
  }

  @Test
  public void testTimingGetters() {
    assertEquals(60, plugin.getSendIntervalSeconds());
    assertEquals(5, plugin.getCodeTtlMinutes());
  }

  @Test
  public void testGetNameAndCanUse() {
    assertEquals("email", plugin.getName());
    assertTrue(plugin.canUse("anyUser"));
  }

  @Test
  public void testValidateOtpValid() {
    when(otpCache.get("email:john")).thenReturn(OTP_CODE);
    assertTrue(plugin.validateOtp("john", OTP_CODE));
    verify(otpSendLockCache, never()).remove(anyString());
  }

  /**
   * A wrong code keeps the send lock of the email method: the email change
   * and API key flows wait out the send interval, as before
   */
  @Test
  public void testValidateOtpInvalid() {
    when(otpCache.get("email:john")).thenReturn("999999");
    assertFalse(plugin.validateOtp("john", OTP_CODE));
    verify(otpSendLockCache, never()).remove(anyString());
  }

  @Test
  public void testValidateOtpIgnoresOtherPurposeCode() {
    when(otpCache.get("john")).thenReturn(OTP_CODE);
    when(otpCache.get("accountDeactivationEmail:john")).thenReturn(OTP_CODE);
    assertFalse(plugin.validateOtp("john", OTP_CODE));
  }

  @Test
  public void testGenerateOtpCodeSuccess() throws Exception {
    plugin.generateOtpCode("john");

    verify(otpCache).put(eq("email:john"), endsWith(":" + OTP_CODE));
    ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
    verify(mailService).sendMessage(messageCaptor.capture());
    String body = String.valueOf(messageCaptor.getValue().getContent());
    assertTrue("OTP code should be injected in the email body", body.contains(OTP_CODE));
    assertTrue("User full name should be injected in the email body", body.contains("John Doe"));
    assertFalse("All i18n placeholders should be resolved", body.contains("${"));
    verify(otpSendLockCache).put(eq("email:john"), anyString());
    assertEquals("The code of the email method goes to the address of the account only",
                 "john@example.com",
                 InternetAddress.toString(messageCaptor.getValue().getAllRecipients()));
  }

  @Test
  public void testACodeRequestedWhileAnotherIsBeingSentIsRefused() throws Exception {
    Map<String, String> sendLocks = mapBackedSendLocks();
    CountDownLatch inSend = new CountDownLatch(1);
    CountDownLatch releaseSend = new CountDownLatch(1);
    AtomicInteger sends = new AtomicInteger();
    doAnswer(invocation -> {
      if (sends.incrementAndGet() == 1) {
        inSend.countDown();
        releaseSend.await(10, TimeUnit.SECONDS);
      }
      return null;
    }).when(mailService).sendMessage(any(MimeMessage.class));

    Thread first = new Thread(() -> plugin.generateOtpCode("john"));
    first.start();
    try {
      assertTrue(inSend.await(10, TimeUnit.SECONDS));
      assertThrows(IllegalStateException.class, () -> plugin.generateOtpCode("john"));
    } finally {
      releaseSend.countDown();
      first.join(10000);
    }
    assertEquals(1, sends.get());
    assertTrue(sendLocks.containsKey("email:john"));
  }

  @Test
  public void testAFailedSendReleasesTheSendLock() throws Exception {
    Map<String, String> sendLocks = mapBackedSendLocks();
    Map<String, String> codes = mapBackedCodes();
    doThrow(new IllegalStateException("smtp down")).when(mailService).sendMessage(any(MimeMessage.class));

    assertThrows(IllegalStateException.class, () -> plugin.generateOtpCode("john"));
    assertFalse(sendLocks.containsKey("email:john"));
    assertTrue(codes.isEmpty());
  }

  @Test
  public void testAFailedSendKeepsTheLockAndTheCodeAnotherRequestWrote() throws Exception {
    Map<String, String> sendLocks = mapBackedSendLocks();
    Map<String, String> codes = mapBackedCodes();
    doAnswer(invocation -> {
      // another request took the lock and stored its code meanwhile
      sendLocks.put("email:john", "another lock");
      codes.put("email:john", "another code");
      throw new IllegalStateException("smtp down");
    }).when(mailService).sendMessage(any(MimeMessage.class));

    assertThrows(IllegalStateException.class, () -> plugin.generateOtpCode("john"));
    assertEquals("another lock", sendLocks.get("email:john"));
    assertEquals("another code", codes.get("email:john"));
  }

  @Test
  public void testTheSendLockIsCheckedAndTakenInOneStep() throws Exception {
    Map<String, String> sendLocks = new ConcurrentHashMap<>();
    CountDownLatch firstInCheck = new CountDownLatch(1);
    CountDownLatch secondInCheck = new CountDownLatch(1);
    AtomicInteger checks = new AtomicInteger();
    when(otpSendLockCache.get(anyString())).thenAnswer(invocation -> {
      String sendLock = sendLocks.get(invocation.getArgument(0, String.class));
      if (checks.incrementAndGet() == 1) {
        firstInCheck.countDown();
        // holds the first check open for up to a second
        secondInCheck.await(1, TimeUnit.SECONDS);
      } else {
        secondInCheck.countDown();
      }
      return sendLock;
    });
    doAnswer(invocation -> sendLocks.put(invocation.getArgument(0, String.class),
                                         invocation.getArgument(1, String.class))).when(otpSendLockCache)
                                                                                    .put(anyString(), anyString());
    AtomicInteger sends = new AtomicInteger();
    doAnswer(invocation -> sends.incrementAndGet()).when(mailService).sendMessage(any(MimeMessage.class));

    Thread first = new Thread(() -> plugin.generateOtpCode("john"));
    first.start();
    try {
      assertTrue(firstInCheck.await(10, TimeUnit.SECONDS));
      assertThrows(IllegalStateException.class, () -> plugin.generateOtpCode("john"));
    } finally {
      first.join(10000);
    }
    assertEquals(1, sends.get());
  }

  private Map<String, String> mapBackedCodes() {
    Map<String, String> codes = new ConcurrentHashMap<>();
    when(otpCache.get(anyString())).thenAnswer(invocation -> codes.get(invocation.getArgument(0, String.class)));
    doAnswer(invocation -> codes.put(invocation.getArgument(0, String.class),
                                     invocation.getArgument(1, String.class))).when(otpCache).put(anyString(), anyString());
    doAnswer(invocation -> codes.remove(invocation.getArgument(0, String.class))).when(otpCache).remove(anyString());
    return codes;
  }

  private Map<String, String> mapBackedSendLocks() {
    Map<String, String> sendLocks = new ConcurrentHashMap<>();
    when(otpSendLockCache.get(anyString())).thenAnswer(invocation -> sendLocks.get(invocation.getArgument(0, String.class)));
    doAnswer(invocation -> sendLocks.put(invocation.getArgument(0, String.class),
                                         invocation.getArgument(1, String.class))).when(otpSendLockCache)
                                                                                    .put(anyString(), anyString());
    doAnswer(invocation -> sendLocks.remove(invocation.getArgument(0, String.class))).when(otpSendLockCache)
                                                                                      .remove(anyString());
    return sendLocks;
  }

  @Test
  public void testGenerateOtpCodeThrottled() throws Exception {
    when(otpSendLockCache.get("email:john")).thenReturn("");

    assertThrows(IllegalStateException.class, () -> plugin.generateOtpCode("john"));
    verify(otpCache, never()).put(anyString(), anyString());
    verify(mailService, never()).sendMessage(any(MimeMessage.class));
  }

  /**
   * The code lifetime is the plugin's, whatever the lifetime the cache
   * declaration gives its entries
   */
  @Test
  public void testACodePastItsLifetimeDoesNotValidate() {
    when(otpCache.get("email:john")).thenReturn((System.currentTimeMillis() - 1000) + ":" + OTP_CODE);
    assertFalse(plugin.validateOtp("john", OTP_CODE));
    when(otpCache.get("email:john")).thenReturn((System.currentTimeMillis() + 60000) + ":" + OTP_CODE);
    assertTrue(plugin.validateOtp("john", OTP_CODE));
  }

  @Test
  public void testACodePastItsLifetimeIsNotConsumed() {
    Map<String, String> codes = storeCodesIn(new HashMap<>());
    codes.put("email:john", (System.currentTimeMillis() - 1000) + ":" + OTP_CODE);
    assertFalse(plugin.consumeOtp("john", OTP_CODE));
  }

  @Test
  public void testGeneratedCodeAndSendLockCarryThePluginLifetimes() throws Exception {
    long before = System.currentTimeMillis();
    plugin.generateOtpCode("john");
    long after = System.currentTimeMillis();

    ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
    verify(otpCache).put(eq("email:john"), code.capture());
    long codeExpiry = Long.parseLong(code.getValue().substring(0, code.getValue().indexOf(':')));
    assertTrue(codeExpiry >= before + 5 * 60000 && codeExpiry <= after + 5 * 60000);

    ArgumentCaptor<String> lock = ArgumentCaptor.forClass(String.class);
    verify(otpSendLockCache).put(eq("email:john"), lock.capture());
    long lockExpiry = Long.parseLong(lock.getValue());
    assertTrue(lockExpiry >= before + 60000 && lockExpiry <= after + 60000);
  }

  @Test
  public void testAnExpiredSendLockLetsANewCodeBeSent() throws Exception {
    when(otpSendLockCache.get("email:john")).thenReturn(String.valueOf(System.currentTimeMillis() - 1000));
    plugin.generateOtpCode("john");
    verify(mailService).sendMessage(any(MimeMessage.class));
  }

  private Map<String, String> storeCodesIn(Map<String, String> codes) {
    lenient().when(otpCache.get(anyString())).thenAnswer(invocation -> codes.get(invocation.getArgument(0)));
    lenient().when(otpCache.remove(anyString())).thenAnswer(invocation -> codes.remove(invocation.getArgument(0)));
    return codes;
  }

  private String liveCode(String code) {
    return (System.currentTimeMillis() + 60000) + ":" + code;
  }

}
