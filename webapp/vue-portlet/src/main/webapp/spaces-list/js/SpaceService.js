export function getSpaces(query, offset, limit, filter) {
  return fetch(`/portal/rest/v1/social/spaces?q=${query || ''}&offset=${offset || 0}&limit=${limit|| 0}&all=${filter === 'all'}&returnSize=true`, {
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

export function leave(spaceId) {
  return fetch(`/portal/rest/homepage/intranet/spaces/leave/${spaceId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function cancel(spaceId) {
  return fetch(`/portal/rest/homepage/intranet/spaces/cancel/${spaceId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function join(spaceId) {
  return fetch(`/portal/rest/homepage/intranet/spaces/join/${spaceId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function requestJoin(spaceId) {
  return fetch(`/portal/rest/homepage/intranet/spaces/request/${spaceId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function accept(spaceId) {
  return fetch(`/portal/rest/homepage/intranet/spaces/accept/${spaceId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function deny(spaceId) {
  return fetch(`/portal/rest/homepage/intranet/spaces/deny/${spaceId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}
