export function getIdentityById(identityId, expand) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/identities/${identityId}?expand=${expand || ''}`, {
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
  const url = remoteId.includes('/') ?
    `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/identities/byParams?providerId=${providerId}&remoteId=${remoteId}`
    : `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/identities/${providerId}/${remoteId}`;
  return fetch(url, {
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
