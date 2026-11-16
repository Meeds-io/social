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

import java.util.Set;

/**
 * The OTP methods whose code proves access to the account itself: each sends
 * its code to the account's own address. A flow that validates a code with a
 * method its client names accepts only these ones. A method whose code goes to
 * an address the caller chooses, as {@link EmailChangeOtpPlugin#NAME}, proves
 * nothing about the account and is never one of them; a new method that proves
 * the account is added here.
 */
public final class AccountOtpMethods {

  private static final Set<String> ACCOUNT_PROOF_METHODS = Set.of("email", "accountDeactivationEmail", "userAccountEmail");

  private AccountOtpMethods() {
  }

  /**
   * @param otpMethod an OTP method name, as a client names it
   * @return true when a code of this method proves access to the account
   */
  public static boolean provesTheAccount(String otpMethod) {
    return otpMethod != null && ACCOUNT_PROOF_METHODS.contains(otpMethod);
  }

}
