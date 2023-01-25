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
export function updateBrandingInformation(branding){
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/platform/branding`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(branding),
  }).then((resp) => {
    if (!resp || !resp.ok) {
      return throwErrorFromServerCall(resp, 'Error saving Company Name');
    }
  });
}

export function getBrandingInformation() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/platform/branding`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => resp.json());
}

export function throwErrorFromServerCall(serverResponse, defaultErrorMessage) {
  if (!serverResponse || !serverResponse.ok) {
    const contentType = serverResponse && serverResponse.headers && serverResponse.headers.get('content-type');
    if (contentType && contentType.indexOf('application/json') !== -1) {
      return serverResponse.json().then((error) => {
        const message = getMessageFromServerError(error, defaultErrorMessage);
        throw new Error(message);
      });
    }
  }
  throw new Error(defaultErrorMessage);
}

export function getMessageFromServerError(error, defaultMessage) {
  if (!error || !error.code || !error.suffix || !error.message) {
    return defaultMessage;
  }
  return error.message;
}