/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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

export function getSpaceTemplates(includeDisabled) {
  return fetch(`/social/rest/space/templates?includeDisabled=${includeDisabled || false}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error when retrieving space templates');
    } else {
      return resp.json();
    }
  });
}

export function getSpaceTemplate(id) {
  return fetch(`/social/rest/space/templates/${id}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Error when retrieving space template');
    } else {
      return resp.json();
    }
  });
}

export function createSpaceTemplate(spaceTemplate) {
  return fetch('/social/rest/space/templates', {
    credentials: 'include',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(spaceTemplate),
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when creating space template');
    }
  });
}

export function updateSpaceTemplate(spaceTemplate) {
  return fetch(`/social/rest/space/templates/${spaceTemplate.id}`, {
    credentials: 'include',
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(spaceTemplate),
  }).then((resp) => {
    if (!resp?.ok) {
      throw new Error('Error when updating space template');
    }
  });
}

export function deleteSpaceTemplate(id) {
  return fetch(`/social/rest/space/templates/${id}`, {
    credentials: 'include',
    method: 'DELETE',
  }).then((resp) => {
    if (!resp?.ok) {
      throw new Error('Error when deleting space template');
    }
  });
}
