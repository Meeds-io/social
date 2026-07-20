
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

export function isOrganizationalUnit(groupId) {
  const params = new URLSearchParams({groupId}).toString();
  return fetch(`/social/rest/organizational-units?${params}`, {
    method: 'GET',
    credentials: 'include',
  }).then(response => {
    if (!response?.ok) {
      throw new Error('Error checking group Organizational Unit status');
    }
    return response.json();
  });
}

export function getMyOrganizationalUnits() {
  return fetch('/social/rest/organizational-units/mine', {
    method: 'GET',
    credentials: 'include',
  }).then(response => {
    if (!response?.ok) {
      throw new Error('Error fetching Organizational Units');
    }
    return response.json();
  });
}

export function updateOrganizationalUnit(groupId, organizationalUnit) {
  const params = new URLSearchParams({groupId, organizationalUnit}).toString();
  return fetch(`/social/rest/organizational-units?${params}`, {
    method: 'PUT',
    credentials: 'include',
  }).then(response => {
    if (!response?.ok) {
      throw new Error('Error updating group Organizational Unit status');
    }
  });
}
