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

// The attributes the registration form lists, read from the store by the
// server for the holder of the page's registration token, with no session.
// An unknown or expired token reads none: the server answers the
// registration with the same 404, which the form shows. A token whose
// account exists reads the list, and its registration is redirected to the
// login page
export function getRegistrationFormSettings(token) {
  return fetch(`/social/${eXo.env.portal.rest}/login/registrationFields?token=${encodeURIComponent(token || '')}`, {
    credentials: 'include',
  }).then(resp => {
    if (resp?.status === 404) {
      return Promise.resolve([]);
    } else if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
    return resp.json();
  }).then(settings => settings || []);
}

// The attributes the onboarding page lists and the values the link's account
// holds for them, read from the store by the server for the holder of the
// page's onboarding token, with no session. A link used or expired since the
// page's own check reads no field: the page's save then reports the link
// expired
export function getOnboardingFormFields(token) {
  return fetch(`/social/${eXo.env.portal.rest}/login/onboardingFields?token=${encodeURIComponent(token || '')}`, {
    credentials: 'include',
  }).then(resp => {
    if (resp?.status === 404) {
      return Promise.resolve(null);
    } else if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
    return resp.json();
  }).then(answer => ({
    fields: answer?.fields || [],
    values: answer?.values || {},
  }));
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

// The server's rules applied to the values an administrator changed on an
// account, against the values its profile holds, without writing them
export function validateUserFields(userName, values) {
  return Vue.prototype.$userAccountConfirmationService.confirmedFetch(`${REST_URL}/v1/social/users/${encodeURIComponent(userName)}/form-fields/validate`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(values || []),
  }).then(readOutcome);
}

// Writes the values an administrator changed on an account: an attribute sent
// empty clears its value
export function saveUserFields(userName, values) {
  return Vue.prototype.$userAccountConfirmationService.confirmedFetch(`${REST_URL}/v1/social/users/${encodeURIComponent(userName)}/form-fields?complete=false`, {
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
