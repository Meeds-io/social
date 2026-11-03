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

// The rules the server applies to an account's fields at Create and Update
// (portal's UserFieldValidator with its defaults, the product's default
// password policy), so the field tells them when it is left. A deployment
// that loosens a rule through gatein.validators.* meets these defaults here,
// as on the change-password drawer; a stricter one is still the server's
const MIN_LENGTH = 3;

const MAX_LENGTH = 255;

// starts and ends with a lowercase letter or a digit of any script, as the
// server's Character.isDigit, single '.', '_', '-' or '@' between them; a
// digit outside the basic plane passes here and is refused by the server,
// which reads chars
const USERNAME_PATTERN = /^[a-z\p{Nd}](?:[._\-@]?[a-z\p{Nd}])*$/u;

// letters, spaces, apostrophes and hyphens; a letter outside the basic
// plane passes here and is refused by the server, which reads chars
const NAME_PATTERN = /^[\p{L}\p{Z}'-]+$/u;

// the server's default email rule, which the shared $utils.isValidEmail does
// not match: it lets through a symbol or an accent in the local part and a
// hyphen in a later domain label, which the server refuses
const EMAIL_PATTERN = /^[-+_A-Za-z0-9]+(\.[-_A-Za-z0-9]+)*@[-A-Za-z0-9]+(\.[A-Za-z0-9]+)*(\.[A-Za-z]{2,})$/;

// 9 to 256 characters with a digit, a lowercase and an uppercase letter
const PASSWORD_PATTERN = /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{9,256}$/;

function withinLength(text) {
  return text.length >= MIN_LENGTH && text.length <= MAX_LENGTH;
}

/**
 * The rules of the account fields, checked when the administrator leaves a
 * field, before the server applies them. Uniqueness, and a policy a
 * deployment configures, stay the server's: its refusal is put on the field
 * by the drawer and cleared by the next input only
 */
export default {
  methods: {
    // The message of the rule the value breaks, null when it passes or when
    // it is blank, which the disabled button already tells
    fieldRuleError(field, value) {
      const text = field === 'password' ? (value || '') : (value || '').trim();
      if (!text) {
        return null;
      }
      switch (field) {
      case 'userName':
        return withinLength(text) && USERNAME_PATTERN.test(text) ? null : this.$t('UsersManagement.creation.invalidUsername');
      case 'firstName':
      case 'lastName':
        return withinLength(text) && NAME_PATTERN.test(text) ? null : this.$t('UsersManagement.creation.invalidName');
      case 'email':
        return withinLength(text) && EMAIL_PATTERN.test(text) ? null : this.$t('UsersManagement.creation.invalidEmail');
      case 'password':
        return PASSWORD_PATTERN.test(text) ? null : this.$t('UsersManagement.creation.passwordCondition');
      default:
        return null;
      }
    },
  },
};
