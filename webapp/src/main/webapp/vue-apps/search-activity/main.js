import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SearchActivityCard');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

export function formatSearchResult(result) {
  return result;
}
Vue.prototype.$utils.includeExtensions('ActivityStreamExtension');

// hide the TopBar loading started by the activity stream (dependency) initialization
document.dispatchEvent(new CustomEvent('hideTopBarLoading'));

