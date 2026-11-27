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

export function getHiddenApplications(lang) {
  return fetch(`/social/rest/hidden-applications${lang && `?lang=${encodeURIComponent(lang)}` || ''}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error while retrieving the hidden applications');
    }
    return resp.json();
  });
}

export function hide(type, name) {
  return fetch(`/social/rest/hidden-applications/${encodeURIComponent(type)}/${encodeURIComponent(name)}`, {
    method: 'PUT',
    credentials: 'include',
  }).then(resp => {
    if (resp?.status === 400) {
      // The refusal's message code, which the caller shows through its label
      return resp.text().then(code => {
        throw new Error(code || 'Error while hiding the application');
      });
    } else if (!resp?.ok) {
      throw new Error('Error while hiding the application');
    }
  });
}

export function show(type, name) {
  return fetch(`/social/rest/hidden-applications/${encodeURIComponent(type)}/${encodeURIComponent(name)}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error while showing the application');
    }
  });
}
