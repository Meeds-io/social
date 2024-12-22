/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
export function saveGroupsSpaceBindings(spaceId, groupNames) {
  return fetch(`${Vue.prototype.$spacesConstants.SPACE_GROUP_BINDING_API}/saveGroupsSpaceBindings/${spaceId}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify(groupNames)
  });
}

export function getGroupSpaceBindings(spaceId) {
  return fetch(`${Vue.prototype.$spacesConstants.SPACE_GROUP_BINDING_API}/${spaceId}`, {credentials: 'include'}).then(resp => resp.json());
}

export function removeBinding(bindingId) {
  return fetch(`${Vue.prototype.$spacesConstants.SPACE_GROUP_BINDING_API}/removeGroupSpaceBinding/${bindingId}`, {
    credentials: 'include',
    method: 'delete'});
}

export function getBindingReportOperations(spaceId) {
  return fetch(`${Vue.prototype.$spacesConstants.SPACE_GROUP_BINDING_API}/getBindingReportOperations?spaceId=${spaceId || ''}`, {credentials: 'include'}).then(resp => resp.json());
}

export function getReport(spaceId, action, groupId, groupBindingId) {
  window.open(`${Vue.prototype.$spacesConstants.SPACE_GROUP_BINDING_API}/getExport?spaceId=${spaceId}&action=${action}&group=${groupId}&groupBindingId=${groupBindingId}`, '_blank');
}
