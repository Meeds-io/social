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
