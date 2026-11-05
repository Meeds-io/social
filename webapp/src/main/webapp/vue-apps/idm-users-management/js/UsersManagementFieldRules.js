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

// The rules the server applies to an account's fields at Create and Update
// (portal's UserFieldValidator with the product's gatein.validators.*
// values: no minimum length for a username or a name beyond a non-blank
// value, 255 at most for each, 3 for an email, which its pattern already
// implies, and the default password policy), so the field tells them when
// it is left. A deployment that loosens a rule further meets these values
// here, as on the change-password drawer; a stricter one is still the
// server's
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

function withinMaxLength(text) {
  return text.length <= MAX_LENGTH;
}

/**
 * The label key of the rule the value breaks, null when it passes or when
 * it is blank, which the disabled button already tells. Checked when the
 * administrator leaves a field, before the server applies the same rules.
 * Uniqueness, and a policy a deployment configures, stay the server's: its
 * refusal is put on the field by the drawer and cleared by the next input
 * only
 *
 * @param {string} field the field name: userName, firstName, lastName, email or password
 * @param {string} value the field value
 * @returns {string} the label key of the broken rule, null when none
 */
export function fieldRuleError(field, value) {
  const text = field === 'password' ? (value || '') : (value || '').trim();
  if (!text) {
    return null;
  }
  switch (field) {
  case 'userName':
    return withinMaxLength(text) && USERNAME_PATTERN.test(text) ? null : 'UsersManagement.creation.invalidUsername';
  case 'firstName':
  case 'lastName':
    return withinMaxLength(text) && NAME_PATTERN.test(text) ? null : 'UsersManagement.creation.invalidName';
  case 'email':
    return withinMaxLength(text) && EMAIL_PATTERN.test(text) ? null : 'UsersManagement.creation.invalidEmail';
  case 'password':
    return PASSWORD_PATTERN.test(text) ? null : 'UsersManagement.creation.passwordCondition';
  default:
    return null;
  }
}
