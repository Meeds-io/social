export function getSettings() {
  return fetch('/portal/rest/mfa/settings', {credentials: 'include'})
    .then((resp) => resp && resp.ok && resp.json());
}
