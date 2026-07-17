import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SearchPageCard');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

export function formatSearchResult(result) {
  return result;
}
