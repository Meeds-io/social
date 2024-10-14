export function getStreamFilter() {
  return localStorage.getItem(`activity-stream-stored-filter-${eXo.env.portal.spaceId || 'all'}`) || 'all_stream';
}

export function setStreamFilter(streamFilter) {
  localStorage.setItem(`activity-stream-stored-filter-${eXo.env.portal.spaceId || 'all'}`, streamFilter);
}