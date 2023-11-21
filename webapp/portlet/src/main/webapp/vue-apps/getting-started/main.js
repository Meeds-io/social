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

const parentAppElement = document.querySelector('#GettingStartedPortlet');
const parentAppElementBtn = document.querySelector('#GettingStartedPortlet .btClose');
if (parentAppElement) {
  if (parentAppElementBtn) {
    parentAppElementBtn.onclick = () => {
      hideGettingStarted().then(() => {
        const parentElementToHide = parentAppElement.closest('.PORTLET-FRAGMENT');
        hideGettingStarted().then(() => parentElementToHide.classList.add('hidden'));
      });
    };
  }
} else {
  document.querySelector('#GettingStartedContainerChildren .PORTLET-FRAGMENT').classList.add('hidden');
}

if (parentAppElement && parentAppElement.dataset.canClose === 'true') {
  parentAppElement.closest('.PORTLET-FRAGMENT').classList.add('hidden');
}
