export function getFavorites(offset, limit) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/favorites?offset=${offset || 0}&limit=${limit|| 0}`, {
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

export function addFavorite(objectType, objectId, objectTitle, parentObjectId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/favorites/${objectType}/${objectId}?objectTitle=${objectTitle}&parentObjectId=${parentObjectId || ''}&ignoreWhenExisting=true`, {
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
