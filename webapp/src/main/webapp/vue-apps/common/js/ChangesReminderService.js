export function isAcknowledged(name) {
  return fetch(`/social/rest/changes-reminder/${encodeURIComponent(name)}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.text();
    }
  }).then(acknowledged => acknowledged === 'true');
}

export function acknowledge(name) {
  return fetch(`/social/rest/changes-reminder/${encodeURIComponent(name)}`, {
    method: 'PUT',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}
