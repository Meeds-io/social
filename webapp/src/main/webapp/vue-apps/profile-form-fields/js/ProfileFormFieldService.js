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

const REST_URL = `${eXo.env.portal.context}/${eXo.env.portal.rest}`;

// The attributes the account form lists, read from the store by the server
export function getAccountFormSettings() {
  return fetch(`${REST_URL}/v1/social/profile/settings?form=account`, {
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
    return resp.json();
  }).then(answer => answer?.settings || []);
}

// The server's rules applied to the values of an account to create, without
// writing them; the session confirmation is asked for when it is missing
export function validateAccountFields(values) {
  return Vue.prototype.$userAccountConfirmationService.confirmedFetch(`${REST_URL}/v1/social/users/form-fields/validate?complete=true`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(values || []),
  }).then(readOutcome);
}

// Writes the values of an account just created
export function saveAccountFields(userName, values) {
  return Vue.prototype.$userAccountConfirmationService.confirmedFetch(`${REST_URL}/v1/social/users/${encodeURIComponent(userName)}/form-fields?complete=true`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(values || []),
  }).then(readOutcome);
}

// A refusal names its code and the refused attribute
function readOutcome(resp) {
  if (resp.ok) {
    return {ok: true};
  } else if (resp.status === 400) {
    return resp.json()
      .catch(() => null)
      .then(refusal => ({ok: false, status: 400, refusal}));
  } else {
    return {ok: false, status: resp.status};
  }
}
