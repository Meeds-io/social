/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
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

export function getLabel(objectType,objectId,language) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/profile/label/${objectType}/${objectId}/${language}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error');
    } else {
      return resp.json();
    }
  });
}

export function getLabels(objectType,objectId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/profile/label/bulk/${objectType}/${objectId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error');
    } else {
      return resp.json();
    }
  });
}

export function addLabels(labels) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/profile/label/bulk`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(labels, (key, value) => {
      if (value !== null) { return value; }
    }),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('labels.create.error.message');
    } else {
      return resp.ok;
    }
  });
}

export function updateLabels(labels) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/profile/label/bulk`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(labels, (_key, value) => {
      if (value !== null) { return value; }
    }),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('labels.update.error.message');
    } else {
      return resp.ok;
    }
  });
}

export function deleteLabels(labels) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/profile/label/bulk`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(labels, (_key, value) => {
      if (value !== null) { return value; }
    }),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('labels.update.error.message');
    } else {
      return resp.ok;
    }
  });
}