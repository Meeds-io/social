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
