/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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

let reactionOptionsPromise = null;

export function getReactionOptions(objectType) {
  if (!reactionOptionsPromise) {
    reactionOptionsPromise = fetch('/social/rest/reactions/options', {
      method: 'GET',
      credentials: 'include',
    }).then(resp => {
      if (!resp?.ok) {
        reactionOptionsPromise = null;
        throw new Error('Error while retrieving reaction options');
      }
      return resp.json();
    });
  }
  return reactionOptionsPromise
    .then(options => objectType && options.filter(option => !option.objectTypes?.length || option.objectTypes.includes(objectType)) || options);
}

export function getReactions(objectType, objectId, reactionId, offset, limit) {
  const reactionFilter = reactionId && `&reactionId=${reactionId}` || '';
  return fetch(`/social/rest/reactions/${objectType}/${objectId}?offset=${offset || 0}&limit=${limit || 0}${reactionFilter}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error while retrieving reactions');
    }
    return resp.json();
  });
}

export function setReaction(objectType, objectId, reactionId) {
  return fetch(`/social/rest/reactions/${objectType}/${objectId}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({reactionId}),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error while setting reaction');
    }
  });
}

export function deleteReaction(objectType, objectId) {
  return fetch(`/social/rest/reactions/${objectType}/${objectId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error while deleting reaction');
    }
  });
}
