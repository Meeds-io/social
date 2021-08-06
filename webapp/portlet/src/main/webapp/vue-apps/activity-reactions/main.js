import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ActivityReactions');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}