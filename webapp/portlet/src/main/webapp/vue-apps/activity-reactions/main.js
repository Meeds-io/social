import './initComponents.js';
import * as extensions from './extensions.js';

// get overrided components if exists
extensions.registerActivityReactionTabs();
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ActivityReactions');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}