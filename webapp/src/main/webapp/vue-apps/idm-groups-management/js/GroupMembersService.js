export function buildFetchLink(groupId, keyword, filter) {
  const form = new FormData();
  form.append('isDisabled', !filter?.status || filter?.status === 'ENABLED' ? 'false':'true');
  form.append('searchEmail', 'true');
  form.append('searchUserName', 'true');
  form.append('userType', filter?.type || '');
  form.append('status', filter?.status || 'ENABLED');
  form.append('q', filter?.status === 'DISABLED' || !keyword ? '' : keyword);
  if (filter?.connectionStatus) {
    form.append('isConnected', filter?.connectionStatus);
  }
  if (filter?.enrollmentStatus) {
    form.append('enrollmentStatus', filter?.enrollmentStatus);
  }
  if (groupId) {
    form.append('groupId', groupId);
  }
  const params = new URLSearchParams(form).toString();
  return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users?${params}`;
}
export function getGroupMembers(groupId, keyword, filter, offset, limit) {
  const fetchLink = buildFetchLink(groupId, keyword, filter);
  return fetch(`${fetchLink}&offset=${offset || 0}&limit=${limit}&returnSize=true`, {
    method: 'GET',
    credentials: 'include',
  })
    .then(resp => {
      if (!resp || !resp.ok) {
        throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
      } else {
        return resp.json();
      }
    });
}
