/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
export function getLockHolders(objectType, objectId) {
  return fetch(`/social/rest/coediting/${objectType}/${objectId}/locks`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when checking whether Object is locked or not');
    }
  });
}

export function getRevision(objectType, objectId) {
  return fetch(`/social/rest/coediting/${objectType}/${objectId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (resp?.ok) {
      return resp.json().catch(() => null);
    } else {
      throw new Error('Error when getting Object revision');
    }
  });
}

export function setLock(objectType, objectId, revision) {
  return fetch(`/social/rest/coediting/${objectType}/${objectId}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    credentials: 'include',
    body: `revision=${revision}`,
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error when setting currently editing revision for current user');
    }
  });
}

export function removeRevision(objectType, objectId) {
  return fetch(`/social/rest/coediting/${objectType}/${objectId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error when removing locked Object');
    }
  });
}
