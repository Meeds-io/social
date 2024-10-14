export function getFavorites(offset, limit,returnSize) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/favorites?offset=${offset || 0}&limit=${limit|| 10}&returnSize=${returnSize}`, {
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

export function addFavorite(objectType, objectId, parentObjectId, spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/favorites/${objectType}/${objectId}?parentObjectId=${parentObjectId || ''}&spaceId=${spaceId || 0}&ignoreWhenExisting=true`, {
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
