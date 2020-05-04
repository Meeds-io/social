import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpaceMenu');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

Vue.use(Vuetify);
const vuetify = new Vuetify({
  dark: true,
  iconfont: '',
});

const appId = 'SpaceMenuApplication';

export function init(settings) {
  document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

  new Vue({
    data: () => ({
      navigations: settings && settings.navigations,
      selectedNavigationUri: settings && settings.selectedNavigationUri,
    }),
    template: `<space-menu id="${appId}" :navigations="navigations" :selected-navigation-uri="selectedNavigationUri" />`,
    vuetify,
  }).$mount(`#${appId}`);
}
