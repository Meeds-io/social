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
let reactionOptions = null;

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
    }).then(options => reactionOptions = options);
  }
  return reactionOptionsPromise
    .then(options => filterReactionOptions(options, objectType));
}

export function getCachedReactionOptions(objectType) {
  return reactionOptions && filterReactionOptions(reactionOptions, objectType) || null;
}

function filterReactionOptions(options, objectType) {
  return objectType && options.filter(option => !option.objectTypes?.length || option.objectTypes.includes(objectType)) || options;
}

export function getReactions(objectType, objectId, reactionId, offset, limit) {
  const reactionFilter = reactionId && `&reactionId=${encodeURIComponent(reactionId)}` || '';
  return fetch(`/social/rest/reactions/${encodeURIComponent(objectType)}/${encodeURIComponent(objectId)}?offset=${offset || 0}&limit=${limit || 0}${reactionFilter}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error while retrieving reactions');
    }
    return resp.json();
  });
}

const SUMMARY_BATCH_SIZE = 100;
const pendingSummaries = {};

/**
 * Retrieves the counts and the reaction of the current user on an object. The
 * requests made in the same tick, by the reactions of every item of a list for
 * example, are sent as one request per object type.
 *
 * @param {String} objectType object type
 * @param {String|Number} objectId object identifier
 * @return {Promise} resolved with {counts, userReactionId}, or with null when the
 *         object doesn't exist or can't be viewed
 */
export function getReactionSummary(objectType, objectId) {
  const id = String(objectId);
  return new Promise((resolve, reject) => {
    let pending = pendingSummaries[objectType];
    if (!pending) {
      pending = pendingSummaries[objectType] = {callbacks: {}};
      setTimeout(() => sendSummaries(objectType), 0);
    }
    (pending.callbacks[id] = pending.callbacks[id] || []).push({resolve, reject});
  });
}

function sendSummaries(objectType) {
  const pending = pendingSummaries[objectType];
  delete pendingSummaries[objectType];
  const ids = Object.keys(pending.callbacks);
  for (let from = 0; from < ids.length; from += SUMMARY_BATCH_SIZE) {
    const batch = ids.slice(from, from + SUMMARY_BATCH_SIZE);
    const objectIds = batch.map(encodeURIComponent).join(',');
    fetch(`/social/rest/reactions/${encodeURIComponent(objectType)}/summary?objectIds=${objectIds}`, {
      method: 'GET',
      credentials: 'include',
    }).then(resp => {
      if (!resp?.ok) {
        throw new Error('Error while retrieving reactions summaries');
      }
      return resp.json();
    }).then(summaries => batch.forEach(id => pending.callbacks[id].forEach(callback => callback.resolve(summaries[id] || null))))
      .catch(error => batch.forEach(id => pending.callbacks[id].forEach(callback => callback.reject(error))));
  }
}

export function setReaction(objectType, objectId, reactionId) {
  return fetch(`/social/rest/reactions/${encodeURIComponent(objectType)}/${encodeURIComponent(objectId)}`, {
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
  return fetch(`/social/rest/reactions/${encodeURIComponent(objectType)}/${encodeURIComponent(objectId)}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error while deleting reaction');
    }
  });
}
