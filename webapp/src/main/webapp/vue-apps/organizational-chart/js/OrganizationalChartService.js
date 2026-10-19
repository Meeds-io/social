export function saveSettings(applicationId, centerUser, hasHeaderTitle) {
  return fetch(`/social/rest/organizational-chart/${encodeURIComponent(applicationId)}/settings`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      centerUser: centerUser || null,
      hasHeaderTitle: !!hasHeaderTitle,
    }),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error');
    }
  });
}
