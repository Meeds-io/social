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

export function saveAttachments(attachmentResource) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments`, {
    headers: {
      'Content-Type': 'application/json'
    },
    method: 'PUT',
    credentials: 'include',
    body: JSON.stringify(attachmentResource)
  }).then(resp => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error(`Error updating attachments, response code = ${resp.status}`);
    }
  });
}

export function getAttachments(objectType, objectId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/${objectType}/${objectId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error(`Error retrieving attachments from server, response code = ${resp.status}`);
    }
  });
}
