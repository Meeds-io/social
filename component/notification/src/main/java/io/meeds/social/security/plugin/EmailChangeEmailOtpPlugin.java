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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.Setter;

/**
 * The code of an email change: sent to the new address, never to the address
 * of the account, and stored bound to that address
 * ({@link EmailChangeOtpPlugin#bindCode(String, String)}), so it proves that
 * the user receives mail there and confirms a change to that address only.
 * Generation, TTL, length and send interval are those of
 * {@link EmailOtpPlugin} (the {@code meeds.apiKey.otp.email.*} settings), as
 * are its caches, under the purpose-bound key of this method: the send lock
 * holds per user whatever the address, and a wrong code keeps it.
 */
@Service
public class EmailChangeEmailOtpPlugin extends EmailOtpPlugin implements EmailChangeOtpPlugin {

  @Value("${meeds.emailChange.otp.email.templatePath:assets/email-change-otp-email-content.html}")
  @Setter
  private String emailChangeEmailBodyPath;

  @Override
  public String getName() {
    return NAME;
  }

  /**
   * Refused: a code of this method goes to the new address only, which the
   * generic send does not name
   */
  @Override
  public void generateOtpCode(String userName) {
    throw new UnsupportedOperationException("The email change code is sent to the new address only");
  }

  @Override
  public void generateOtpCode(String userName, String newEmail) {
    if (StringUtils.isBlank(newEmail)) {
      throw new IllegalArgumentException("The new email address is mandatory");
    }
    sendOtpCode(userName, newEmail.trim());
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

}
