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
package io.meeds.social.core.profileproperty.exception;

import lombok.Getter;

/**
 * A profile attribute value an account or registration form refuses: the
 * message is the refusal's code, which is also its label key, and
 * {@code getPropertyName()} names the refused attribute.
 */
public class ProfileFormFieldException extends IllegalArgumentException {

  /** The attribute is not one the form can write */
  public static final String NOT_ALLOWED      = "profile.formField.notAllowed";

  /** A dropdown value is none of the attribute's options */
  public static final String INVALID_OPTION   = "profile.formField.invalidOption";

  /** A Required attribute has no value */
  public static final String REQUIRED         = "profile.formField.required";

  /** An e-mail attribute's value is not an e-mail address */
  public static final String INVALID_EMAIL    = "profile.formField.invalidEmail";

  /**
   * The value is longer than its storage, or than the group it makes when
   * the attribute is group-synchronized
   */
  public static final String TOO_LONG         = "profile.formField.tooLong";

  /**
   * The value holds a character the storage cannot hold, makes no group,
   * would not read back as written, or is sent in the other attribute's shape:
   * values for a single-valued attribute, value for a multi-valued one
   */
  public static final String INVALID_VALUE    = "profile.formField.invalidValue";

  private static final long  serialVersionUID = -3925866734263421367L;

  @Getter
  private final String       propertyName;

  public ProfileFormFieldException(String code, String propertyName) {
    super(code);
    this.propertyName = propertyName;
  }

}
