import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpaceSettings');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

Vue.use(Vuetify);
const vuetify = new Vuetify({
  dark: true,
  iconfont: '',
});

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`;

const appId = 'SpaceSettings';
const cacheId = `${appId}_${eXo.env.portal.spaceId}`;

export function init(maxUploadSize) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    // get external components
    document.addEventListener('component-external-space-settings-updated', () => {
      if (extensionRegistry) {
        const externalComponents = extensionRegistry.loadComponents('external-space').map(component => component.componentOptions.componentImpl);
        document.dispatchEvent(new CustomEvent('external-components-loaded', { detail: externalComponents }));
      }
    });
    
    appElement.id = appId;

    new Vue({
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<space-settings
                  v-cacheable="{cacheId: '${cacheId}'}"
                  id="${appId}"
                  :max-upload-size="${maxUploadSize}"
                  class="singlePageApplication" />`,
      i18n,
      vuetify,
    }).$mount(appElement);
  });
}