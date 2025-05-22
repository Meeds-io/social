/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
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
function escapeUnicodeInJson(obj) {
  return JSON.stringify(obj).replace(/[\u007f-\uffff]/g, function (c) {
    return `\\u${  (`0000${  c.charCodeAt(0).toString(16)}`).slice(-4)}`;
  });
}

export function saveSettings(saveSettingsURL, settings) {
  const formData = new FormData();
  formData.append('settings', escapeUnicodeInJson(settings));
  const urlParams = new URLSearchParams(formData).toString();

  return fetch(saveSettingsURL, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: urlParams,
  }).then(resp => {
    if (!resp.ok) {
      throw new Error('Error while saving space creation settings');
    }
  });
}