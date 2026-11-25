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

const REST_URL = '/social/rest/logging/loggers';
const STATUS_URL = '/social/rest/logging/status';

function checkResponse(resp, errorMessage) {
  if (resp?.ok) {
    return resp;
  }
  return resp.json().catch(() => ({})).then(body => {
    const error = new Error(body?.message || errorMessage);
    error.status = resp.status;
    throw error;
  });
}

export function getStatus() {
  return fetch(STATUS_URL, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => checkResponse(resp, 'Error when retrieving the status')).then(resp => resp.json());
}

export function getLoggers(options) {
  const params = new URLSearchParams();
  if (options?.query) {
    params.append('query', options.query);
  }
  params.append('explicitOnly', !!options?.explicitOnly);
  params.append('offset', options?.offset || 0);
  params.append('limit', options?.limit || 20);
  return fetch(`${REST_URL}?${params.toString()}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => checkResponse(resp, 'Error when retrieving the loggers')).then(resp => resp.json());
}

export function setLevel(name, level) {
  return fetch(`${REST_URL}/${encodeURIComponent(name)}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      level,
    }),
  }).then(resp => checkResponse(resp, 'Error when setting the level'));
}

export function resetLevel(name) {
  return fetch(`${REST_URL}/${encodeURIComponent(name)}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => checkResponse(resp, 'Error when resetting the level'));
}

export function resetAll() {
  return fetch(REST_URL, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => checkResponse(resp, 'Error when resetting the levels'));
}

window.Object.defineProperty(Vue.prototype, '$loggingLevelService', {
  value: {
    getStatus,
    getLoggers,
    setLevel,
    resetLevel,
    resetAll,
  },
});
