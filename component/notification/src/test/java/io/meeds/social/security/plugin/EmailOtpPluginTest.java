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
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
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
  public void testConsumeOtpRemovesTheCodeOnce() {
    Map<String, String> codes = storeCodesIn(new HashMap<>());
    codes.put("john", liveCode(OTP_CODE));
    assertTrue(plugin.consumeOtp("john", OTP_CODE));
    assertFalse(plugin.consumeOtp("john", OTP_CODE));
    assertFalse(codes.containsKey("john"));
  }

  @Test
  public void testConsumeOtpRefusesAnotherCodeAndKeepsIt() {
    Map<String, String> codes = storeCodesIn(new HashMap<>());
    codes.put("john", liveCode("999999"));
    assertFalse(plugin.consumeOtp("john", OTP_CODE));
    assertTrue("A code that was not consumed must stay valid", plugin.validateOtp("john", "999999"));
  }

  @Test
  public void testConsumeOtpRefusesAnExpiredCode() {
    Map<String, String> codes = storeCodesIn(new HashMap<>());
    codes.put("john", (System.currentTimeMillis() - 1000) + ":" + OTP_CODE);
    assertFalse(plugin.consumeOtp("john", OTP_CODE));
  }

  @Test
  public void testACodeSentBetweenTheReadAndTheRemovalIsPutBack() {
    String newerCode = liveCode("999999");
    when(otpCache.get("john")).thenReturn(liveCode(OTP_CODE));
    when(otpCache.remove("john")).thenReturn(newerCode);
    assertFalse(plugin.consumeOtp("john", OTP_CODE));
    verify(otpCache).put("john", newerCode);
  }

  @Test
  public void testValidateOtpInvalid() {
    when(otpCache.get("john")).thenReturn("999999");
    assertFalse(plugin.validateOtp("john", OTP_CODE));
  }

  @Test
  public void testGenerateOtpCodeSuccess() throws Exception {
    plugin.generateOtpCode("john");

    verify(otpCache).put(eq("john"), endsWith(":" + OTP_CODE));
    verify(mailService).sendMessage(any(MimeMessage.class));
  }

  /**
   * A declared cache refuses a lifetime change at runtime: the code is sent
   * all the same
   */
  @Test
  public void testACodeIsSentWhenTheCacheRefusesALifetimeChange() throws Exception {
    doThrow(new UnsupportedOperationException("The configuration of the cache cannot not be modified")).when(otpCache)
                                                                                                         .setLiveTime(anyLong());
    plugin.setOtpCache(null); // looked up from the cache service, as in production
    plugin.generateOtpCode("john");

    verify(otpCache).put(eq("john"), endsWith(":" + OTP_CODE));
    verify(mailService).sendMessage(any(MimeMessage.class));
  }

  /**
   * The code lifetime is the plugin's, whatever the lifetime the cache
   * declaration gives its entries
   */
  @Test
  public void testACodePastItsLifetimeDoesNotValidate() {
    when(otpCache.get("john")).thenReturn((System.currentTimeMillis() - 1000) + ":" + OTP_CODE);
    assertFalse(plugin.validateOtp("john", OTP_CODE));
    when(otpCache.get("john")).thenReturn((System.currentTimeMillis() + 60000) + ":" + OTP_CODE);
    assertTrue(plugin.validateOtp("john", OTP_CODE));
  }

  @Test
  public void testTheGeneratedCodeCarriesThePluginLifetime() throws Exception {
    long before = System.currentTimeMillis();
    plugin.generateOtpCode("john");
    long after = System.currentTimeMillis();

    ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
    verify(otpCache).put(eq("john"), code.capture());
    long codeExpiry = Long.parseLong(code.getValue().substring(0, code.getValue().indexOf(':')));
    assertTrue(codeExpiry >= before + 5 * 60000 && codeExpiry <= after + 5 * 60000);
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
