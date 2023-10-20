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

export function getSiteById(siteId, expandNavigations, excludeEmptyNavigationSites, lang, visibility, excludeGroupNodesWithoutPageChildNodes, temporalCheck) {
  const formData = new FormData();
  formData.append('lang', lang);
  formData.append('excludeEmptyNavigationSites', excludeEmptyNavigationSites);
  formData.append('expandNavigations', expandNavigations);
  formData.append('excludeEmptyNavigationSites', excludeEmptyNavigationSites);
  formData.append('excludeGroupNodesWithoutPageChildNodes', excludeGroupNodesWithoutPageChildNodes);
  formData.append('temporalCheck', temporalCheck);
  if (visibility) {
    visibility.forEach(visibility => {
      formData.append('visibility', visibility);
    });
  }
  const params = new URLSearchParams(formData).toString();

  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/sites/${siteId}?${params}`, {
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