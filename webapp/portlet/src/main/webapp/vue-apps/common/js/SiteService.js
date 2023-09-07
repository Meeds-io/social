export function getSites(siteType, excludedSiteType, excludedSiteName, excludeEmptyNavigationSites, excludeSpaceSites, expandNavigations, filterByDisplayed, sortByDisplayOrder, displayed, filterByPermissions) {
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
  formData.append('excludeEmptyNavigationSites', excludeEmptyNavigationSites);
  formData.append('excludeSpaceSites', excludeSpaceSites);
  formData.append('expandNavigations', expandNavigations);
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