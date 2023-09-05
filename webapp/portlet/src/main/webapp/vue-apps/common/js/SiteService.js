export function getSites(siteType, excludedSiteName, expandNavigations, displayed, allSites, filterByPermissions) {
  const formData = new FormData();
  if (siteType) {
    formData.append('siteType', siteType);
  }
  if (excludedSiteName) {
    formData.append('excludedSiteName', excludedSiteName);
  }
  formData.append('expandNavigations', expandNavigations);
  if (displayed) {
    formData.append('displayed', displayed);
  }
  formData.append('allSites', allSites);
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