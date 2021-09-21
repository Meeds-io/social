export function addFavorite(type, id, parentId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/metadatas/favorites`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      name: eXo.env.portal.userIdentityId,
      audienceId: eXo.env.portal.userIdentityId,
      objectType: type,
      objectId: id,
      parentObjectId: parentId,
    }),
  }).then(resp => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function removeFavorite(itemId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/metadatas/favorites/${eXo.env.portal.userIdentityId}/${itemId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}
