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
 * Posts the widget preferences to the portlet action URL. The server stores
 * only headerTranslations, showHiddenSubspaces and subspacesLimit, under the
 * parent space management guard.
 *
 * The promise reflects the HTTP transport only: a refusal by the portlet
 * (not a manager, limit out of 1..25, header translations not a JSON object)
 * is a PortletException that the portal logs and answers with a 200, like
 * every JSR-286 action of the sibling widgets. Callers therefore validate
 * the values before posting; the server checks are a defence, not a UX path.
 *
 * @param {string} saveSettingsUrl the portlet action URL
 * @param {object} settings the preference values to store, by name
 * @returns {Promise<void>} resolved once the request completed, rejected on
 *          an HTTP error
 */
export function saveSettings(saveSettingsUrl, settings) {
  const formData = new FormData();
  if (settings) {
    Object.keys(settings).forEach(name => {
      const value = settings[name];
      formData.append(name, typeof value === 'object' ? JSON.stringify(value) : value);
    });
  }
  return fetch(saveSettingsUrl.replaceAll('&amp;', '&'), {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then(resp => {
    if (!resp.ok) {
      throw new Error('Error while saving subspaces list settings');
    }
  });
}
