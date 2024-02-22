/*
    This file is part of the Meeds project (https://meeds.io/).
    Copyright (C) 2022 Meeds Association
    contact@meeds.io
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.
    You should have received a copy of the GNU Lesser General Public License
    along with this program; if not, write to the Free Software Foundation,
    Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

export function getNavigations(siteName, siteType, scope, visibility, exclude, nodeId, expandBreadcrumb, expand) {
  const formData = new FormData();
  if (siteName) {
    formData.append('siteName', siteName);
  }
  if (scope) {
    formData.append('scope', scope);
  }
  if (visibility) {
    visibility.forEach(visibility => {
      formData.append('visibility', visibility);
    });
  }
  if (exclude) {
    formData.append('exclude', exclude);
  }
  if (nodeId) {
    formData.append('nodeId', nodeId);
  }
  if (expandBreadcrumb) {
    formData.append('expandBreadcrumb', expandBreadcrumb);
  }
  if (expand) {
    formData.append('expand', expand);
  }

  const params = new URLSearchParams(formData).toString();
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/navigations/${siteType || 'portal'}?${params}`, {
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