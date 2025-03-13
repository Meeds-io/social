export function hideGettingStarted () {
  return fetch(`${eXo.$spacesConstants.PORTAL_CONTEXT}/${eXo.$spacesConstants.PORTAL_REST}/getting-started`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Error deleting getting started');
    }
  });
}

export function init () {
  const parentAppElement = document.querySelector('#GettingStartedPortlet');
  const parentAppElementBtn = document.querySelector('#GettingStartedPortlet .btClose');
  if (parentAppElement) {
    if (parentAppElementBtn) {
      parentAppElementBtn.onclick = () => {
        hideGettingStarted().then(() => {
          const parentElementToHide = parentAppElement.closest('.PORTLET-FRAGMENT');
          hideGettingStarted().then(() => eXo.$updateApplicationVisibility(false, parentElementToHide));
        });
      };
    }
  } else {
    eXo.$updateApplicationVisibility(false, document.querySelector('#GettingStartedPortletParent'));
  }
}
