export function getIdentityById(identityId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/identities/${identityId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getIdentityByProviderIdAndRemoteId(providerId, remoteId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/identities/${providerId}/${remoteId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}
