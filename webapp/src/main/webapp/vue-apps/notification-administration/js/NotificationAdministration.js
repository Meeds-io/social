/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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

export function getSettings() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/notifications/settings`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error getting notification settings');
    }
  });
}

export function saveSenderEmail(name, email) {
  const formData = new FormData();
  formData.append('name', name);
  formData.append('email', email);
  const params = new URLSearchParams(formData).toString();
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/notifications/settings`, {
    method: 'PATCH',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: params
  }).then(resp => {
    if (!resp?.ok) {
      if (resp?.status === 400) {
        return resp.text().then(e => {
          throw new Error(e);
        });
      } else {
        throw new Error('Error saving plugin settings');
      }
    }
  });
}

export function savePluginSettings(pluginId, channels) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/notifications/settings/plugin/${pluginId}`, {
    method: 'PATCH',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: `channels=${channels}`
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error saving plugin settings');
    }
  });
}

export function saveChannelStatus(channelId, enable) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/notifications/settings/channel/${channelId}`, {
    method: 'PATCH',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: `enable=${enable}`
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error saving channel setting');
    }
  });
}
