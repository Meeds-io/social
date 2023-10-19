export function getSiteById(siteId, expandNavigations, excludeEmptyNavigationSites, lang) {
  const formData = new FormData();
  formData.append('lang', lang);
  formData.append('excludeEmptyNavigationSites', excludeEmptyNavigationSites);
  formData.append('expandNavigations', expandNavigations);

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