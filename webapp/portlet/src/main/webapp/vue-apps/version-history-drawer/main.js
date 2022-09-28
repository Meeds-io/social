import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('VersionHistoryDrawer');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}
const appId = 'VersionHistoryDrawer';

Vuetify.prototype.preset = eXo.env.portal.vuetifyPreset;
Vue.use(Vuetify);

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`;

export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    Vue.createApp({
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, appId, 'Version History drawer');
  });
}