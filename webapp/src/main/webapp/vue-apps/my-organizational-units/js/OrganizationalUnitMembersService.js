export function getOrganizationalUnitMembers(groupId, keyword, offset, limit) {
  const form = new FormData();
  form.append('groupId', groupId);
  form.append('includeInheritedMemberships', 'true');
  form.append('isDisabled', 'false');
  form.append('searchEmail', 'true');
  form.append('searchUserName', 'true');
  form.append('status', 'ENABLED');
  form.append('q', keyword || '');
  form.append('offset', offset || 0);
  form.append('limit', limit);
  form.append('returnSize', true);
  const params = new URLSearchParams(form).toString();
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users?${params}`, {
    method: 'GET',
    credentials: 'include',
  }).then(response => {
    if (!response?.ok) {
      throw new Error('Error fetching Organizational Unit users');
    }
    return response.json();
  });
}
