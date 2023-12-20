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

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`;

const appId = 'SpaceSettings';

export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => 
    Vue.createApp({
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: '<space-settings />',
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'Space Settings')
  ).finally(() => Vue.prototype.$utils.includeExtensions('SpaceSettingExtension'));
}
