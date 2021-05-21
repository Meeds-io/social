import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ShareActivityDrawer');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

Vuetify.prototype.preset = eXo.env.portal.vuetifyPreset;

Vue.use(Vuetify);