export function getSite(siteType, siteName, lang) {
  const formData = new FormData();
  formData.append('lang', lang);
  formData.append('siteName', siteName);
  const params = new URLSearchParams(formData).toString();
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/sites/details/${siteType}?${params}`, {
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