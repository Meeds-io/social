export function getStreamFilter(appId) {
  return localStorage.getItem(`activity-stream-stored-filter-${appId}`) || 'all_stream';
}

export function setStreamFilter(streamFilter, appId) {
  localStorage.setItem(`activity-stream-stored-filter-${appId}`, streamFilter);
}