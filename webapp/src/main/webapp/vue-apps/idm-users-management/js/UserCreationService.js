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
export const MESSAGE_MAX_LENGTH = 500;

const REFUSED_FIELDS = {
  USERNAME: 'userName',
  FIRSTNAME: 'firstName',
  LASTNAME: 'lastName',
  EMAIL: 'email',
  PASSWORD: 'password',
  LANGUAGE: 'language',
  ROLE: 'role',
};

export function getSupportedLanguages() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/social/translations/configuration`, {
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
    return resp.json();
  }).then(configuration => {
    const supported = configuration?.supportedLanguages || {};
    return {
      defaultLanguage: configuration?.defaultLanguage?.replace('-', '_') || null,
      languages: Object.keys(supported)
        .map(value => ({value, text: supported[value]}))
        .sort((a, b) => a.text.localeCompare(b.text)),
    };
  });
}

export function getOnboardingSettings() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users/onboardingSettings`, {
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
    return resp.json();
  });
}

/**
 * Creates the account, after the session's confirmation when the server
 * asks for it
 *
 * @param {object} account the account as the form describes it
 * @returns {Promise<object>} the outcome: `{ userName }` when created,
 *          `{ status, code }` when the server refused or did not complete it,
 *          the code being null for any other status
 */
export function createAccount(account) {
  // The shared confirmation service of the common bundle, the one every
  // other guarded call of the application goes through, so that one
  // pending confirmation covers them all: never an import of its module,
  // which would bundle a second instance with its own dialog
  return Vue.prototype.$userAccountConfirmationService.confirmedFetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(account),
  }).then(resp => {
    if (resp?.status === 201) {
      return resp.json().then(created => ({userName: created.userName}));
    } else if (resp?.status === 400 || resp?.status === 500) {
      return resp.text().then(code => ({status: resp.status, code}));
    } else {
      // Another status (a session expired, a proxy error): no code, the
      // drawer shows its generic error
      return {status: resp?.status, code: null};
    }
  });
}

/**
 * Sends the onboarding email of a created account with the administrator's
 * message
 *
 * @param {string} userName the login of the created account
 * @param {string} message the message shown above the link, may be blank
 * @returns {Promise<boolean>} true when the email was sent
 */
export function sendInvitation(userName, message) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/onboard/${encodeURIComponent(userName)}`, {
    method: 'PATCH',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      message: (message || '').trim(),
    }),
  }).then(resp => !!resp?.ok)
    .catch(() => false);
}

/**
 * Reads a 400 code: `<PREFIX>:<detail>`, the prefix naming the refused field
 *
 * @param {string} code the response body
 * @returns {object} `{ field, prefix, detail }`, or null when the code names
 *          no field
 */
export function parseRefusal(code) {
  const [prefix, ...rest] = (code || '').split(':');
  const field = REFUSED_FIELDS[prefix];
  return field ? {field, prefix, detail: rest.join(':')} : null;
}

/**
 * Reads a 500 code of an account created without all its parts:
 * `USER:CREATED_INCOMPLETE[_ENABLED]:<login>:<parts>`
 *
 * @param {string} code the response body
 * @returns {object} `{ userName, missing, deactivated }`, or null when the
 *          code is another one
 */
export function parseIncomplete(code) {
  const parts = (code || '').split(':');
  if (parts[0] !== 'USER' || (parts[1] !== 'CREATED_INCOMPLETE' && parts[1] !== 'CREATED_INCOMPLETE_ENABLED')) {
    return null;
  }
  return {
    userName: parts[2],
    missing: (parts[3] || '').split(',').filter(p => p),
    deactivated: parts[1] === 'CREATED_INCOMPLETE',
  };
}
