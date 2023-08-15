export function getPortletBanner(uploadFileId) {
  const formData = new FormData();
  formData.append('uploadFileId', uploadFileId);

  const params = new URLSearchParams(formData).toString();
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/portletBanner?${params}`, {
    credentials: 'include',
    method: 'GET',
    headers: {
      'Content-Type': 'application/json'
    },
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error when retrieving portlet banner');
    }
  });
}

export function saveSettings(saveSettingsURL, settings) {
  const formData = new FormData();
  if (settings) {
    Object.keys(settings).forEach(name => {
      formData.append(name, settings[name]);
    });
  }
  return fetch(saveSettingsURL, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
} 

