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

import java.security.SecureRandom;
import java.util.Map;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.web.security.security.SecureRandomService;

import io.meeds.social.core.mail.BrandedEmailSender;
import io.meeds.web.security.plugin.OtpPlugin;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * Generates, caches and validates email OTP codes: the code is stored under a
 * purpose-bound cache key ({@link #getCacheKey(String)}) and the email is sent
 * through the common {@link BrandedEmailSender} with a per-plugin content
 * fragment and subject key.
 */
@Service
public class EmailOtpPlugin implements OtpPlugin {

  private static final String      OTP_CACHE_NAME           = "otp.email";

  private static final String      OTP_SEND_LOCK_CACHE_NAME = "otp.email.sendLock";

  @Autowired
  private CacheService             cacheService;

  @Autowired
  private SecureRandomService      secureRandomService;

  @Autowired
  private ResourceBundleService    resourceBundleService;

  @Autowired
  @Setter // Used in Tests only
  private BrandedEmailSender       brandedEmailSender;

  /**
   * OTP Tentatives Cache in minutes
   */
  @Value("${meeds.apiKey.otp.email.ttl:5}")
  @Getter
  @Setter
  private long                     otpTtl;

  @Value("${meeds.apiKey.otp.email.length:5}")
  @Getter
  @Setter
  private long                     otpLength;

  @Value("${meeds.apiKey.otp.email.templatePath:assets/otp-email-content.html}")
  @Getter
  @Setter
  private String                   emailBodyPath;

  /**
   * Minimal delay in seconds between two OTP email sends for a same user
   */
  @Value("${meeds.apiKey.otp.email.sendInterval:60}")
  @Getter
  @Setter
  private long                     sendInterval;

  private SecureRandom             secureRandom;

  @Setter
  private ExoCache<String, String> otpCache;

  @Setter
  private ExoCache<String, String> otpSendLockCache;

  @Override
  public String getName() {
    return "email";
  }

  @Override
  public boolean canUse(String userName) {
    return true;
  }

  @Override
  public boolean validateOtp(String userName, String otpCode) {
    return StringUtils.equals(otpCode, getOtpCache().get(getCacheKey(userName)));
  }

  @Override
  @SneakyThrows
  public void generateOtpCode(String userName) {
    String cacheKey = getCacheKey(userName);
    if (getOtpSendLockCache().get(cacheKey) != null) {
      throw new IllegalStateException(String.format("OTP email already sent to user %s less than %s seconds ago",
                                                    userName,
                                                    sendInterval));
    }
    String lang = brandedEmailSender.getUserLang(userName);
    String emailSubject = resourceBundleService.getSharedString(getEmailSubjectKey(), LocaleUtils.toLocale(lang));
    String otpCode = generateOtpCode();
    getOtpCache().put(cacheKey, otpCode);
    brandedEmailSender.sendEmail(userName,
                                 emailSubject,
                                 getEmailBodyPath(),
                                 Map.of("####", otpCode,
                                        "$CODE_EXPIRATION_LABEL", getCodeExpirationLabel(lang)));
    getOtpSendLockCache().put(cacheKey, StringUtils.EMPTY);
  }

  /**
   * Key under which the OTP code of a user is stored, distinct by plugin so
   * that a code generated for one purpose can never validate another one
   */
  protected String getCacheKey(String userName) {
    return getName() + ":" + userName;
  }

  protected String getEmailSubjectKey() {
    return "otp.email.subject";
  }

  private String getCodeExpirationLabel(String lang) {
    return resourceBundleService.getSharedString("otp.email.label.codeExpirationMessage", LocaleUtils.toLocale(lang))
                                .replace("{0}", String.valueOf(otpTtl));
  }

  private String generateOtpCode() {
    return String.valueOf(getSecureRandom().nextLong((long) Math.pow(10d, otpLength - 1d),
                                                     (long) Math.pow(10d, otpLength)));
  }

  private SecureRandom getSecureRandom() {
    if (secureRandom == null) {
      secureRandom = secureRandomService.getSecureRandom();
    }
    return secureRandom;
  }

  private ExoCache<String, String> getOtpCache() {
    if (otpCache == null) {
      otpCache = cacheService.getCacheInstance(OTP_CACHE_NAME);
      otpCache.setLiveTime(otpTtl * 60);
    }
    return otpCache;
  }

  private ExoCache<String, String> getOtpSendLockCache() {
    if (otpSendLockCache == null) {
      otpSendLockCache = cacheService.getCacheInstance(OTP_SEND_LOCK_CACHE_NAME);
      otpSendLockCache.setLiveTime(sendInterval);
    }
    return otpSendLockCache;
  }

}
