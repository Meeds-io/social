export function updateSpace(spaceId, model) {
  return fetch(`/portal/rest/v1/social/spaces/${spaceId}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(model),
  }).then(resp => {
    if (!resp || !resp.ok) {
      return resp.text().then((text) => {
        throw new Error(text);
      });
    } else {
      return resp.json();
    }
  });
}
