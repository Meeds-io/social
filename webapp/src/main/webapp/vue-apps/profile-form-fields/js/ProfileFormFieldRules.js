/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

// The profile e-mail rule, on the whole value: $utils.isValidEmail, which the
// profile's e-mail fields apply, and the browser's type=email grammar (the
// WHATWG valid e-mail address), which the account forms do not run
const INPUT_EMAIL_PATTERN = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;

// The size of the column an attribute is stored in
export const MAX_STORED_LENGTH = 2000;

export function hasValue(value) {
  return Array.isArray(value) ? value.some(entry => hasValue(entry)) : !!String(value ?? '').trim();
}

export function isValidEmail(value) {
  return Vue.prototype.$utils.isValidEmail(value) && INPUT_EMAIL_PATTERN.test(value);
}

// An approximation of the stored form's length, as a courtesy before the
// server, which counts org.json's escapes too
export function storedLength(value) {
  if (Array.isArray(value)) {
    return JSON.stringify(value.filter(hasValue).map(entry => ({value: String(entry)}))).length;
  }
  return String(value ?? '').length;
}
