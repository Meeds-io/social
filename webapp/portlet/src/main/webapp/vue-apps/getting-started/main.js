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

const parentAppElement = document.querySelector('#GettingStartedPortlet .btClose');
if (parentAppElement) {
  parentAppElement.onclick = () => {
    hideGettingStarted().then(() => {
      const parentElementToHide = parentAppElement.closest('.PORTLET-FRAGMENT');
      hideGettingStarted().then(() => parentElementToHide.classList.add('hidden'));
    });
  };
}
const parentElementToHide = document.querySelector('#GettingStartedPortlet');
if (parentElementToHide && parentElementToHide.dataset.canClose === 'true') {
  parentElementToHide.closest('.PORTLET-FRAGMENT').classList.add('hidden');
}
