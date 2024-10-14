/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

export function getObservedObjects(offset, limit) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/social/observers?offset=${offset || 0}&limit=${limit|| 10}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function isObserved(objectType, objectId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/social/observers/${objectType}/${objectId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.text().then(v => v === 'true');
    }
  });
}

export function createObserver(objectType, objectId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/social/observers/${objectType}/${objectId}`, {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    method: 'POST',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function deleteObserver(objectType, objectId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/social/observers/${objectType}/${objectId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}
