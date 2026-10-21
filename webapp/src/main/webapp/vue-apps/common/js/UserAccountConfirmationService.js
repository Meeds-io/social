/*
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

/**
 * Body of the 403 the server answers to an administrator action from a
 * session not confirmed yet with a one-time password
 */
export const OTP_REQUIRED = 'OTP_REQUIRED';

/**
 * Message of the error a call rejects with when the administrator closes the
 * confirmation dialog without confirming: nothing was applied, and nothing is
 * to be shown
 */
export const CONFIRMATION_CANCELLED = 'CONFIRMATION_CANCELLED';

export const OTP_METHOD = 'userAccountEmail';

let pendingConfirmation = null;

let confirmationDialog = null;

/**
 * Performs the fetch, and when the server answers that the session must be
 * confirmed, asks the administrator for the one-time password, then replays
 * the same request once. Any other response is returned as is, and so is the
 * response of the replay, a second refusal included.
 *
 * @param {String} url request URL
 * @param {Object} init fetch options, replayed as they are: the body must be
 *   a string, a FormData or a URLSearchParams, never a stream
 * @returns {Promise<Response>} the final response; rejects with an Error whose
 *   message is CONFIRMATION_CANCELLED when the dialog is closed unconfirmed
 */
export async function confirmedFetch(url, init) {
  const response = await fetch(url, init);
  if (!await isOtpRequired(response)) {
    return response;
  }
  await requestConfirmation();
  return fetch(url, init);
}

/**
 * Opens the confirmation dialog, once for every request refused meanwhile
 *
 * @returns {Promise} resolved once the session is confirmed, rejected with
 *   CONFIRMATION_CANCELLED when the dialog is closed unconfirmed
 */
export function requestConfirmation() {
  if (!pendingConfirmation) {
    pendingConfirmation = getConfirmationDialog()
      .then(dialog => dialog.confirm())
      .finally(() => pendingConfirmation = null);
  }
  return pendingConfirmation;
}

export function isCancelled(error) {
  return error?.message === CONFIRMATION_CANCELLED;
}

export function sendCode() {
  return fetch(`/social/rest/otp?method=${OTP_METHOD}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error(String(resp?.status || 'error'));
    }
  });
}

export function getSettings() {
  return fetch(`/social/rest/otp/settings?method=${OTP_METHOD}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error(String(resp?.status || 'error'));
    }
    return resp.json();
  });
}

export function confirmSession(otpCode) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users/confirmation`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams({otpCode}).toString(),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error(String(resp?.status || 'error'));
    }
  });
}

async function isOtpRequired(response) {
  if (response?.status !== 403) {
    return false;
  }
  const body = await response.clone().text();
  return body === OTP_REQUIRED;
}

function getConfirmationDialog() {
  if (!confirmationDialog) {
    const lang = eXo?.env?.portal?.language || 'en';
    confirmationDialog = exoi18n.loadLanguageAsync(lang, [`/social/i18n/locale.portlet.Portlets?lang=${lang}`])
      .then(i18n => {
        let parentElement = document.querySelector('#vuetify-apps') || document.querySelector('#body-end-container');
        if (!parentElement) {
          parentElement = document.createElement('div');
          document.body.appendChild(parentElement);
        }
        const dialogElement = document.createElement('div');
        parentElement.appendChild(dialogElement);
        const app = new Vue({
          vuetify: Vue.prototype.vuetifyOptions,
          i18n,
          render: h => h('v-app', {class: 'transparent', attrs: {id: 'user-account-confirmation'}}, [
            h('user-account-confirmation-dialog', {ref: 'dialog'}),
          ]),
        }).$mount(dialogElement);
        return app.$refs.dialog;
      })
      .catch(error => {
        confirmationDialog = null;
        throw error;
      });
  }
  return confirmationDialog;
}
