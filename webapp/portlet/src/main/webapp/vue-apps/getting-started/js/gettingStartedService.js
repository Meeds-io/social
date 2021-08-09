export function getGettingStartedSteps(){
  return fetch(`${Vue.prototype.$spacesConstants.PORTAL_CONTEXT}/${Vue.prototype.$spacesConstants.PORTAL_REST}/getting-started`, {
    method: 'GET',
    credentials: 'include'
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error setting getting started  settings');
    }
  });
}

export function hideGettingStarted() {
  return fetch(`${Vue.prototype.$spacesConstants.PORTAL_CONTEXT}/${Vue.prototype.$spacesConstants.PORTAL_REST}/getting-started`, {
    method: 'DELETE',
    credentials: 'include',
  }).then((resp) => {
    if (!resp || !resp.ok) {
      throw new Error('Error deleting getting started');
    }
  });
}