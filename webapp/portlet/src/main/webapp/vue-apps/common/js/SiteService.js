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


export function getSites(siteType, excludedSiteType, excludedSiteName, excludeEmptyNavigationSites, excludeSpaceSites, expandNavigations, filterByDisplayed, sortByDisplayOrder, displayed, filterByPermissions, excludeGroupNodesWithoutPageChildNodes, temporalCheck, visibility) {
  const formData = new FormData();
  if (siteType) {
    formData.append('siteType', siteType);
  }
  if (excludedSiteType) {
    formData.append('excludedSiteType', excludedSiteType);
  }

  if (excludedSiteName) {
    formData.append('excludedSiteName', excludedSiteName);
  }
  formData.append('lang', eXo.env.portal.language);
  formData.append('excludeEmptyNavigationSites', excludeEmptyNavigationSites);
  formData.append('excludeGroupNodesWithoutPageChildNodes', excludeGroupNodesWithoutPageChildNodes);
  formData.append('temporalCheck', temporalCheck);
  formData.append('excludeSpaceSites', excludeSpaceSites);
  formData.append('expandNavigations', expandNavigations);
  if (visibility) {
    visibility.forEach(visibility => {
      formData.append('visibility', visibility);
    });
  }
  formData.append('filterByDisplayed', filterByDisplayed);
  formData.append('sortByDisplayOrder', sortByDisplayOrder);
  if (filterByDisplayed) {
    formData.append('displayed', displayed);
  }
  formData.append('filterByPermissions', filterByPermissions);
  const params = new URLSearchParams(formData).toString();

  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/sites?${params}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error while getting sites');
    }
  });
}

export function getSiteById(siteId, params) {
  const formData = new FormData();
  formData.append('lang', params.lang);
  formData.append('excludeEmptyNavigationSites', params.excludeEmptyNavigationSites);
  formData.append('expandNavigations', params.expandNavigations);
  formData.append('excludeEmptyNavigationSites', params.excludeEmptyNavigationSites);
  formData.append('excludeGroupNodesWithoutPageChildNodes', params.excludeGroupNodesWithoutPageChildNodes);
  formData.append('temporalCheck', params.temporalCheck);
  if (params.visibility) {
    params.visibility.forEach(visibility => {
      formData.append('visibility', visibility);
    });
  }
  const dataParams = new URLSearchParams(formData).toString();

  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/sites/${siteId}?${dataParams}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error while getting site by id');
    }
  });
}