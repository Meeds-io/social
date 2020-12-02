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

Vue.use(Vuetify);
const vuetify = new Vuetify({
  dark: true,
  iconfont: '',
});

const appId = 'SpaceMenu';
const cacheId = `${appId}_${eXo.env.portal.spaceId}`;

const appIdAction = 'SpaceTitleActionComponnetsContainer';
const cachedIdActions = `${appIdAction}_${eXo.env.portal.spaceId}`;

export function init(settings) {
  document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

  const appElement = document.createElement('div');
  appElement.id = appId;

  new Vue({
    data: () => ({
      navigations: settings && settings.navigations,
      selectedNavigationUri: settings && settings.selectedNavigationUri,
    }),
    mounted() {
      document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
    },
    template: `<space-menu v-cacheable="{cacheId: '${cacheId}'}" id="${appId}" :navigations="navigations" :selected-navigation-uri="selectedNavigationUri" />`,
    vuetify,
  }).$mount(appElement);

  new Vue({
    mounted() {
      document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
    },
    template: `<space-title-action-components v-cacheable="{cacheId: '${cachedIdActions}'}" id="${appIdAction}" />`,
    vuetify,
  }).$mount(appIdAction);

}
