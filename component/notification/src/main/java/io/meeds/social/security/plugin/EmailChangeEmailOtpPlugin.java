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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import lombok.Getter;
import lombok.Setter;

/**
 * The code of an email change: sent to the new address, never to the address
 * of the account, and stored bound to that address
 * ({@link EmailChangeOtpPlugin#bindCode(String, String)}), so it proves that
 * the user receives mail there and confirms a change to that address only.
 * Generation, TTL, length and send interval are those of
 * {@link EmailOtpPlugin} (the {@code meeds.apiKey.otp.email.*} settings), as
 * are its caches, under the purpose-bound key of this method. The send lock
 * is taken first, on every request, refused ones included, and holds per
 * user whatever the address; a wrong code keeps it. Wrong guesses are
 * bounded as the OTP service bounds them, per user over
 * {@code meeds.apiKey.otp.maxTentatives.ttl}
 * ({@link #validateOtp(String, String)}). Once the change is
 * written, the former address is told of it
 * ({@link #sendChangeNotice(String, String)}).
 */
@Service
public class EmailChangeEmailOtpPlugin extends EmailOtpPlugin implements EmailChangeOtpPlugin {

  private static final Log    LOG                 = ExoLogger.getLogger(EmailChangeEmailOtpPlugin.class);

  private static final String TENTATIVES_CACHE_NAME = "otp.emailChange.tentatives";

  /**
   * Serializes the read, increment and write of the wrong-guess count:
   * concurrent guesses would otherwise count as one
   */
  private final Object        attemptsLock          = new Object();

  @Autowired
  private CacheService        tentativesCacheService;

  @Value("${meeds.apiKey.otp.maxTentatives.count:5}")
  @Getter
  @Setter
  private long                maxAttempts;

  /** Minutes a wrong-guess count lives after the last wrong guess */
  @Value("${meeds.apiKey.otp.maxTentatives.ttl:5}")
  @Getter
  @Setter
  private long                attemptsTtl;

  private ExoCache<String, Long> tentativesCache;

  @Value("${meeds.emailChange.otp.email.templatePath:assets/email-change-otp-email-content.html}")
  @Setter
  private String           emailChangeEmailBodyPath;

  @Value("${meeds.emailChange.notice.email.templatePath:assets/email-change-notice-email-content.html}")
  @Setter
  private String           emailChangeNoticeBodyPath;

  @Override
  public String getName() {
    return NAME;
  }

  /**
   * Refused: a code of this method goes to the new address only, which the
   * generic send does not name
   *
   * @throws IllegalArgumentException always
   */
  @Override
  public void generateOtpCode(String userName) {
    throw new IllegalArgumentException("The email change code is sent to the new address only");
  }

  @Override
  public <R> R generateOtpCode(String userName, String newEmail, AddressCheck<R> addressCheck) throws Exception {
    takeSendLock(userName);
    R refusal = addressCheck.check(newEmail);
    if (refusal != null) {
      return refusal;
    }
    if (StringUtils.isBlank(newEmail)) {
      throw new IllegalArgumentException("The new email address is mandatory");
    }
    sendOtpCodeUnderLock(userName, newEmail.trim());
    return null;
  }

  /**
   * Bounds the wrong guesses at the user's code as the OTP service bounds the
   * other methods: per user, {@code meeds.apiKey.otp.maxTentatives.count} of
   * them, in a count that lives {@code meeds.apiKey.otp.maxTentatives.ttl}
   * minutes after the last one and that a valid code clears. At the limit the
   * code is dropped, and every code is refused until the count expires: a new
   * send does not start it again. The OTP service's own count does not bound
   * this code: it is cleared by any valid code, and the email change checks the
   * code of the current address in the same request. Guesses are counted one
   * at a time on a server, so concurrent requests never count as one.
   */
  @Override
  public boolean validateOtp(String userName, String otpCode) {
    synchronized (attemptsLock) {
      Long count = getTentativesCache().get(userName);
      long attempts = count == null ? 0 : count;
      if (attempts >= maxAttempts) {
        return false;
      }
      boolean valid = super.validateOtp(userName, otpCode);
      if (valid) {
        getTentativesCache().remove(userName);
      } else {
        getTentativesCache().put(userName, attempts + 1);
        if (attempts + 1 >= maxAttempts) {
          getOtpCache().remove(getCacheKey(userName));
        }
      }
      return valid;
    }
  }

  /**
   * Best effort: a mail server failure is logged and never undoes the change
   * the user confirmed with both codes
   */
  @Override
  public void sendChangeNotice(String userName, String formerEmail) {
    if (StringUtils.isBlank(formerEmail)) {
      return;
    }
    try {
      sendEmail(userName, formerEmail.trim(), "social.emailChange.notice.email.subject", emailChangeNoticeBodyPath);
    } catch (Exception e) {
      LOG.warn("Error sending the email change notice of user {} to the former address", userName, e);
    }
  }

  @Override
  public String getEmailBodyPath() {
    return emailChangeEmailBodyPath;
  }

  @Override
  protected String getEmailSubjectKey() {
    return "social.emailChange.otp.email.subject";
  }

  @Override
  protected String getStoredCode(String otpCode, String recipientEmail) {
    return EmailChangeOtpPlugin.bindCode(otpCode, recipientEmail);
  }

  private ExoCache<String, Long> getTentativesCache() {
    if (tentativesCache == null) {
      tentativesCache = tentativesCacheService.getCacheInstance(TENTATIVES_CACHE_NAME);
      tentativesCache.setLiveTime(attemptsTtl * 60);
    }
    return tentativesCache;
  }

}
