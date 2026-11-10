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

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import io.meeds.web.security.plugin.OtpPlugin;

/**
 * The OTP method of the email change: the code goes to the new address, and
 * it confirms a change to that address only. It is stored bound to the
 * address it was sent to ({@link #bindCode(String, String)}), so the flow
 * validates and consumes it through {@link io.meeds.web.security.service.OtpService}
 * with the bound value of the code typed and of the address being set, never
 * with the code alone.
 */
public interface EmailChangeOtpPlugin extends OtpPlugin {

  String NAME = "emailChange";

  /**
   * Takes the send lock of the user first, then runs the check of the
   * address, then sends a new code to the address when the check accepts it.
   * The lock holds for the send interval whatever the check answers, so a
   * refused address is rate-limited like a send, and a request made while the
   * lock holds is refused before the address is checked.
   *
   * @param <R> the type of the check's refusal
   * @param userName the user changing their email
   * @param newEmail the address to set
   * @param addressCheck answers the refusal of the address, or null to send
   * @return the check's refusal, or null when the code was sent
   * @throws IllegalStateException when the lock is already held: nothing is
   *           checked or sent
   * @throws Exception when the check fails
   */
  <R> R generateOtpCode(String userName, String newEmail, AddressCheck<R> addressCheck) throws Exception;

  /**
   * The check of the new address, run under the send lock
   *
   * @param <R> the type of its refusal
   */
  @FunctionalInterface
  interface AddressCheck<R> {

    /**
     * @param email the address to set
     * @return the refusal of the address, or null when it is accepted
     * @throws Exception when the check fails
     */
    R check(String email) throws Exception;

  }

  /**
   * The value a code of this method is stored, validated and consumed as: the
   * code and the address, lower-cased and trimmed. A generated code holds
   * digits only, so a typed code that holds anything else binds to nothing and
   * the value is unambiguous: a code sent to one address never confirms
   * another.
   *
   * @param otpCode the code, as sent or as typed
   * @param email the address it was sent to, or the address being set
   * @return the bound value, null when the code or the address is blank or
   *         the code is not made of digits
   */
  static String bindCode(String otpCode, String email) {
    if (StringUtils.isBlank(email) || !StringUtils.isNumeric(otpCode)) {
      return null;
    }
    return otpCode + ":" + email.trim().toLowerCase(Locale.ROOT);
  }

}
