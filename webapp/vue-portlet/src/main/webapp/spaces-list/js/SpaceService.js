export function getSpaces(query, offset, limit) {
  return fetch(`/portal/rest/v1/social/spaces?q=${query || ''}&offset=${offset || 0}&limit=${limit|| 0}&returnSize=true`)
    .then(resp => resp && resp.ok && resp.json());
}

export function getSpaceMembers(spaceId, offset, limit) {
  return fetch(`/portal/rest/v1/social/spaces/${spaceId}/users?offset=${offset || 0}&limit=${limit|| 0}&returnSize=true`)
    .then(resp => resp && resp.ok && resp.json());
}
