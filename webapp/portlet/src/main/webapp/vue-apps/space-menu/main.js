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
const cacheId = `${appId}_${eXo.env.portal.spaceId}`;

const appIdAction = 'SpaceTitleActionComponnetsContainer';
const cachedIdActions = `${appIdAction}_${eXo.env.portal.spaceId}`;

export function init(settings) {
  const appElement = document.createElement('div');
  appElement.id = appId;

  Vue.createApp({
    data: {
      navigations: settings && settings.navigations,
      selectedNavigationUri: settings && settings.selectedNavigationUri,
    },
    template: `<space-menu v-cacheable="{cacheId: '${cacheId}'}" id="${appId}" :navigations="navigations" :selected-navigation-uri="selectedNavigationUri" />`,
    vuetify: Vue.prototype.vuetifyOptions,
  }, appElement, 'Space Menu');

  const actionsElement = document.createElement('div');
  actionsElement.id = appIdAction;

  new Vue({
    mounted() {
      this.$root.$emit('application-loaded');
    },
    template: `<space-title-action-components v-cacheable="{cacheId: '${cachedIdActions}'}" id="${appIdAction}" />`,
    vuetify: Vue.prototype.vuetifyOptions,
  }).$mount(actionsElement);

}
