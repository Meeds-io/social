/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

const brandingPath = () => `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/platform/branding`;

function readError(resp, defaultMessage) {
  const error = new Error(defaultMessage);
  error.status = resp?.status;
  if (!resp || resp.status === 500 || resp.status === 503) {
    return Promise.resolve(error);
  }
  // a refusal body is the message code (400) or the conflict code (409)
  return resp.text()
    .then(code => {
      error.code = code;
      return error;
    })
    .catch(() => error);
}

/**
 * @returns {Promise<object>} the published themes view: version, branding identity, themes in use and offered modes
 */
export function getThemes() {
  return fetch(`${brandingPath()}/themes`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error loading the themes');
    }
    return resp.json();
  });
}

/**
 * Publishes the whole Branding and Theme page at once.
 * The rejection carries the HTTP status and, for a refusal, the message code in `code`.
 * @param {object} publication the branding publication, holding the version the page loaded
 * @returns {Promise<{version: number}>} the new version
 */
export function publish(publication) {
  return fetch(`${brandingPath()}/publication`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(publication),
  }).then(resp => {
    if (!resp?.ok) {
      return readError(resp, 'Error publishing the branding').then(error => {
        throw error;
      });
    }
    return resp.json();
  });
}

/**
 * Saves the custom CSS by its own request: it is not part of the publication.
 * The rejection carries the HTTP status and, for a refusal, the message code in `code`.
 * @param {string} customCss the custom CSS, blank to remove it
 * @returns {Promise<void>} resolved once saved
 */
export function saveCustomCss(customCss) {
  return fetch(`${brandingPath()}/customCss`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({customCss: customCss || ''}),
  }).then(resp => {
    if (!resp?.ok) {
      return readError(resp, 'Error saving the custom CSS').then(error => {
        throw error;
      });
    }
  });
}

/**
 * @param {object} theme the theme as the page holds it (id or clientId, shared, light, dark, startFrom)
 * @returns {Promise<object>} both versions' resolved values, contrast ratios and warnings
 */
export function resolve(theme) {
  return fetch(`${brandingPath()}/themes/resolve`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(theme),
  }).then(resp => {
    if (!resp?.ok) {
      return readError(resp, 'Error resolving the theme').then(error => {
        throw error;
      });
    }
    return resp.json();
  });
}
