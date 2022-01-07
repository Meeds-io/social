export function initTags(tooltipTitle) {
  const tags = document.querySelectorAll('.metadata-tag:not([title])');
  if (tags && tags.length) {
    tags.forEach(tagItem => {
      tagItem.title = tooltipTitle;
    });
  }
}

export function searchTags(query, limit) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/tags?q=${query || ''}&limit=${limit || 10}`, {
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