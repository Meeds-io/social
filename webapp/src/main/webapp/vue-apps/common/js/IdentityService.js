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

export function getIdentityByProviderIdAndRemoteId(providerId, remoteId, expand) {
  const url = remoteId.includes('/') ?
    `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/identities/byParams?providerId=${providerId}&remoteId=${remoteId}&expand=${expand || ''}`
    : `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/identities/${providerId}/${remoteId}?expand=${expand || ''}`;
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
