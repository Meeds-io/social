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

/**
 * The accounts that never signed in, by Pending Users tab: created and not
 * invited yet, or invited and not connected yet. Enabled accounts only, of
 * any type, as the users table lists them by default
 */
const TAB_FILTERS = {
  creation: 'notEnrolled',
  invitations: 'enrolled',
};

function pendingUsersUrl(tab, offset, limit) {
  const params = new URLSearchParams({
    isDisabled: 'false',
    status: 'ENABLED',
    userType: '',
    isConnected: 'neverConnected',
    enrollmentStatus: TAB_FILTERS[tab],
    offset: String(offset || 0),
    limit: String(limit),
    returnSize: 'true',
  });
  return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users?${params.toString()}`;
}

function fetchPendingUsers(tab, offset, limit) {
  if (!TAB_FILTERS[tab]) {
    return Promise.reject(new Error(`Unknown Pending Users tab ${tab}`));
  }
  return fetch(pendingUsersUrl(tab, offset, limit), {
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
    return resp.json();
  });
}

/**
 * @param {string} tab `creation` or `invitations`
 * @param {number} offset the first row
 * @param {number} limit the page size
 * @returns {Promise<object>} `{ users, size }`
 */
export function getPendingUsers(tab, offset, limit) {
  return fetchPendingUsers(tab, offset, limit)
    .then(data => ({
      users: data?.entities || data?.users || [],
      size: data?.size || 0,
    }));
}

/**
 * @param {string} tab `creation` or `invitations`
 * @returns {Promise<number>} the number of accounts the tab lists, one row
 *          requested
 */
export function countPendingUsers(tab) {
  return fetchPendingUsers(tab, 0, 1).then(data => data?.size || 0);
}
