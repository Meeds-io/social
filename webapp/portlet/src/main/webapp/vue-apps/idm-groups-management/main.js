import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('GroupsManagement');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

//getting language of the PLF
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

const appId = 'GroupsManagement';

//should expose the locale ressources as REST API 
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`;

export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale ressources are ready
    Vue.createApp({
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<groups-management id="${appId}" />`,
      vuetify: Vue.prototype.vuetifyOptions,
      i18n,
    }, `#${appId}`, 'Group Management');
  });
}
