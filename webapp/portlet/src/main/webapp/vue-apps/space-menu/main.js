import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpaceMenu', 'SpaceTitleActionComponents');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

const appId = 'SpaceMenu';

export function init(settings) {
  const appElement = document.createElement('div');
  appElement.id = appId;

  Vue.createApp({
    data: {
      navigations: settings && settings.navigations,
      selectedNavigationUri: settings && settings.selectedNavigationUri,
    },
    template: `<space-menu id="${appId}" :navigations="navigations" :selected-navigation-uri="selectedNavigationUri" />`,
    vuetify: Vue.prototype.vuetifyOptions,
  }, `#${appId}`, 'Space Menu');
}
