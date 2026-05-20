
export function getNestedGroups(groupId, limit, offset) {
  const form = new FormData();
  form.append('groupId', groupId);
  form.append('limit', limit);
  form.append('offset', offset);
  form.append('returnSize', true);
  const params= new URLSearchParams(form).toString();
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups/nested?${params}`, {
    method: 'GET',
    credentials: 'include'
  }).then(response => {
    if (!response?.ok) {
      throw new Error('Error fetching nested groups');
    }
    return response.json();
  });
}
