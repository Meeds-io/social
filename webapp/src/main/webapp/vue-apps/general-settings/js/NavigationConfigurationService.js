/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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

export function getConfiguration() {
  return fetch('/social/rest/navigation/settings', {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when retrieving configuration');
    }
  });
}

export function saveConfiguration(configuration) {
  configuration = JSON.parse(JSON.stringify(configuration));
  configuration.sidebar.items.forEach(item => delete item.items);
  return fetch('/social/rest/navigation/settings', {
    headers: {
      'Content-Type': 'application/json'
    },
    method: 'PUT',
    credentials: 'include',
    body: JSON.stringify(configuration),
  }).then((resp) => {
    if (!resp?.ok) {
      throw new Error('Error when saving configuration');
    }
  });
}
