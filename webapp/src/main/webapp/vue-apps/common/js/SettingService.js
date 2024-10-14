export function setSettingValue(contextKey, contextValue, scopeKey, scopeValue, key, value) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/settings/${contextKey},${contextValue}/${scopeKey},${scopeValue}/${key}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      value: value,
    }),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function getSettingValue(contextKey, contextValue, scopeKey, scopeValue, key) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/settings/${contextKey},${contextValue}/${scopeKey},${scopeValue}/${key}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp?.ok) {
      if (resp.status !== 404) {
        throw new Error('Response code indicates a server error', resp);
      }
    } else {
      return resp.json();
    }
  });
}
