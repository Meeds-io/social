/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.exoplatform.portal.rest.UserAccountService;

import lombok.Setter;

/**
 * Same OTP mechanics as {@link EmailOtpPlugin} (generation, TTL, length, send
 * interval, single-use consumption) and the same email wording, under its
 * own subject, for the confirmation of an administrator session before
 * actions on user accounts.
 * The code is stored under a purpose-bound cache key, so a code sent for
 * another flow never confirms an administrator session.
 */
@Service
public class UserAccountEmailOtpPlugin extends EmailOtpPlugin {

  public static final String NAME = UserAccountService.OTP_METHOD;

  @Value("${meeds.userAccount.otp.email.templatePath:assets/user-account-otp-email-content.html}")
  @Setter
  private String             userAccountEmailBodyPath;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getEmailBodyPath() {
    return userAccountEmailBodyPath;
  }

  @Override
  protected String getEmailSubjectKey() {
    return "social.userAccount.otp.email.subject";
  }

}
