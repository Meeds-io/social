export function getSites(siteType, displayed, allSites, excludedSiteName, expandNavigations, filterByPermission) {
  const formData = new FormData();
  if (siteType) {
    formData.append('siteType', siteType);
  }
  formData.append('displayed', displayed);
  formData.append('allSites', allSites);
  formData.append('filterByPermissions', filterByPermission);
  if (excludedSiteName) {
    formData.append('excludedSiteName', excludedSiteName);
  }
  formData.append('expandNavigations', expandNavigations);
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