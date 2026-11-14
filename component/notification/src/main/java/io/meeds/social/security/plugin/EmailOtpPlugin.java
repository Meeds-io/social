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

  /**
   * A wrong code keeps the send lock, unless the method releases it
   * ({@link #releasesSendLockOnWrongCode()}): the user then may ask for a
   * new code at once instead of waiting out the send interval, bounded by
   * the attempts the validating service counts per code
   */
  @Override
  public boolean validateOtp(String userName, String otpCode) {
    String cacheKey = getCacheKey(userName);
    boolean valid = StringUtils.equals(otpCode, getOtpCache().get(cacheKey));
    if (!valid && releasesSendLockOnWrongCode()) {
      getOtpSendLockCache().remove(cacheKey);
    }
    return valid;
  }

  /**
   * Whether a wrong code lifts the send lock of this method. False here: the
   * email change, API key and account deactivation flows keep waiting out
   * the send interval after a wrong code
   */
  protected boolean releasesSendLockOnWrongCode() {
    return false;
  }

  /**
   * Removes the code from the cache, so that it never validates again. Only
   * the request that removes it wins: a concurrent one finds it gone.
   */
  @Override
  public boolean consumeOtp(String userName, String otpCode) {
    return StringUtils.equals(otpCode, getOtpCache().remove(getCacheKey(userName)));
  }

  @Override
  public long getCodeTtlMinutes() {
    return otpTtl;
  }

  @Override
  public long getSendIntervalSeconds() {
    return sendInterval;
  }

  @Override
  public void generateOtpCode(String userName) {
    sendOtpCode(userName, null);
  }

  /**
   * Sends a new code to the user, under the code key and the send lock of
   * this method: to the given address when there is one, else to the address
   * of the user's account. The code is stored as
   * {@link #getStoredCode(String, String)} answers, the value that
   * {@link #validateOtp(String, String)} and {@link #consumeOtp(String, String)}
   * compare.
   *
   * @param userName the user the code is for
   * @param recipientEmail the address to send the code to, null for the
   *          address of the user's account
   */
  protected void sendOtpCode(String userName, String recipientEmail) {
    String cacheKey = getCacheKey(userName);
    checkSendLock(userName, cacheKey);
    sendCode(userName, recipientEmail, cacheKey);
    getOtpSendLockCache().put(cacheKey, StringUtils.EMPTY);
  }

  /**
   * Takes the send lock of the user at once, before anything is checked or
   * sent: the lock then holds for the send interval whatever the request
   * answers, a refusal included. The code is then sent by
   * {@link #sendOtpCodeUnderLock(String, String)}.
   *
   * @param userName the user the code is for
   * @throws IllegalStateException when the lock is already held
   */
  protected void takeSendLock(String userName) {
    String cacheKey = getCacheKey(userName);
    checkSendLock(userName, cacheKey);
    getOtpSendLockCache().put(cacheKey, StringUtils.EMPTY);
  }

  /**
   * Sends a new code as {@link #sendOtpCode(String, String)} does, under the
   * send lock the caller took with {@link #takeSendLock(String)}. When the send
   * fails, the lock is released and the code dropped: no code left, so the
   * user may ask again at once.
   *
   * @param userName the user the code is for
   * @param recipientEmail the address to send the code to, null for the
   *          address of the user's account
   */
  protected void sendOtpCodeUnderLock(String userName, String recipientEmail) {
    String cacheKey = getCacheKey(userName);
    try {
      sendCode(userName, recipientEmail, cacheKey);
    } catch (Exception e) {
      getOtpCache().remove(cacheKey);
      getOtpSendLockCache().remove(cacheKey);
      throw e;
    }
  }

  private void checkSendLock(String userName, String cacheKey) {
    if (getOtpSendLockCache().get(cacheKey) != null) {
      throw new IllegalStateException(String.format("OTP email already sent to user %s less than %s seconds ago",
                                                    userName,
                                                    sendInterval));
    }
  }

  @SneakyThrows
  private void sendCode(String userName, String recipientEmail, String cacheKey) {
    String lang = brandedEmailSender.getUserLang(userName);
    String emailSubject = resourceBundleService.getSharedString(getEmailSubjectKey(), LocaleUtils.toLocale(lang));
    String otpCode = generateOtpCode();
    getOtpCache().put(cacheKey, getStoredCode(otpCode, recipientEmail));
    Map<String, String> tokens = Map.of("####", otpCode, "$CODE_EXPIRATION_LABEL", getCodeExpirationLabel(lang));
    if (recipientEmail == null) {
      brandedEmailSender.sendEmail(userName, emailSubject, getEmailBodyPath(), tokens);
    } else {
      brandedEmailSender.sendEmail(userName, recipientEmail, emailSubject, getEmailBodyPath(), tokens);
    }
  }

  /**
   * Sends an email with no code to the user, in their language, through the
   * common branded layout: to the given address when there is one, else to
   * the address of the user's account
   *
   * @param userName the user the email is about
   * @param recipientEmail the address to send it to, null for the address of
   *          the user's account
   * @param subjectKey the shared bundle key of its subject
   * @param templatePath the path of its content fragment
   * @throws Exception when the email cannot be built or sent
   */
  protected void sendEmail(String userName, String recipientEmail, String subjectKey, String templatePath) throws Exception {
    String lang = brandedEmailSender.getUserLang(userName);
    String emailSubject = resourceBundleService.getSharedString(subjectKey, LocaleUtils.toLocale(lang));
    brandedEmailSender.sendEmail(userName, recipientEmail, emailSubject, templatePath, Map.of());
  }

  /**
   * The value a sent code is stored as. The code itself here: the email, API
   * key, account deactivation and administrator session methods compare the
   * typed code alone
   *
   * @param otpCode the generated code
   * @param recipientEmail the address the code is sent to, null for the
   *          address of the user's account
   * @return the value to store
   */
  protected String getStoredCode(String otpCode, String recipientEmail) {
    return otpCode;
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

  /**
   * The cache that holds the sent codes, under {@link #getCacheKey(String)},
   * for the code's TTL
   */
  protected ExoCache<String, String> getOtpCache() {
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
