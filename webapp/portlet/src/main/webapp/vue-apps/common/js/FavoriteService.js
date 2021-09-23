export function addFavorite(objectType, objectId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/favorites/${objectType}/${objectId}?ignoreWhenExisting=true`, {
    method: 'POST',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function removeFavorite(objectType, objectId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/favorites/${objectType}/${objectId}?ignoreNotExisting=true`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}
