/**
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

export function getExtensions() {
  return fetch('/social/rest/contentLinks', {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting content link extensions');
    }
  });
}

export function searchLinks(objectType, query, offset, limit) {
  return fetch(`/social/rest/contentLinks/${objectType}/search?query=${query}&offset=${offset}&limit=${limit}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when searching for content links');
    }
  });
}

export function getLink(objectType, objectId) {
  return fetch(`/social/rest/contentLinks/link/${objectType}/${objectId}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting content link');
    }
  });
}

export function saveLinks(objectType, objectId, fieldName, links) {
  return fetch(`/social/rest/contentLinks/${objectType}/${objectId}?fieldName=${fieldName}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({links}),
  }).then((resp) => {
    if (!resp?.ok) {
      throw new Error('Error when saving content links');
    }
  });
}
