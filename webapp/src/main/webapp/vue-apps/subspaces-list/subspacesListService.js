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
 * Asks the portlet resource URL for everything the widget renders from.
 *
 * The envelope is computed server-side: whether the hosting space is a parent
 * space at all, what the viewer may do on it, and the sub-spaces they may see.
 * Whether hidden sub-spaces are part of that list is decided by the portlet
 * preference, never by this call — there is deliberately no parameter for it.
 *
 * @param {string} resourceUrl the portlet resource URL
 * @param {number} limit maximum number of sub-spaces to return; omitted to get
 *        the whole list (the server caps it)
 * @returns {Promise<object>} {parentSpace, canManageSpace, canCreateSubspace,
 *          subspaces[]}
 */
export function getSubspaces(resourceUrl, limit) {
  const url = new URL(resourceUrl.replaceAll('&amp;', '&'), window.location.origin);
  if (limit) {
    url.searchParams.append('limit', limit);
  }
  return fetch(url, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp.ok) {
      throw new Error('Error while retrieving subspaces list');
    }
    return resp.json();
  });
}

/**
 * Builds the URL of a sub-space avatar served by the portlet itself. Used for
 * a HIDDEN sub-space the viewer is not a member of: the space avatar REST
 * endpoint may refuse them, while the portlet serves the avatar of any
 * sub-space it lists to the viewer.
 *
 * Total by construction: an absent resource URL (an older JSP that does not
 * declare it yet) returns nothing rather than throwing, so the caller falls
 * back to the standard avatar URL instead of losing the row.
 *
 * @param {string} avatarResourceUrl the portlet resource URL with id 'avatar'
 * @param {string|number} spaceId the sub-space identifier
 * @returns {string} the image URL, or an empty string when it cannot be built
 */
export function getSubspaceAvatarUrl(avatarResourceUrl, spaceId) {
  if (!avatarResourceUrl || !spaceId) {
    return '';
  }
  const url = new URL(avatarResourceUrl.replaceAll('&amp;', '&'), window.location.origin);
  url.searchParams.append('spaceId', spaceId);
  return url.toString();
}

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
