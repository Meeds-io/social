/*
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

// The enabled users of every type, internal and external, as the users
// management page lists them; the search matches their name
export function getUsers(query, limit, signal) {
  const params = new URLSearchParams({
    q: query || '',
    userType: '',
    status: 'ENABLED',
    isDisabled: 'false',
    offset: '0',
    limit: String(limit || 0),
    returnSize: 'true',
  }).toString();
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users?${params}`, {
    method: 'GET',
    credentials: 'include',
    signal,
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error while retrieving users');
    } else {
      return resp.json();
    }
  });
}
