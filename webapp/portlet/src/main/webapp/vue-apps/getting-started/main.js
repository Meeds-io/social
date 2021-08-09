import './initComponents.js';

import * as gettingStartedService from './js/gettingStartedService.js';

window.Object.defineProperty(Vue.prototype, '$gettingStartedService', {
  value: gettingStartedService,
});

// getting language of the PLF 
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

// should expose the locale ressources as REST API 
const url = `${Vue.prototype.$spacesConstants.PORTAL}/${Vue.prototype.$spacesConstants.PORTAL_REST}/i18n/bundle/locale.portlet.social.GettingStartedPortlet-${lang}.json`;


// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('GettingStarted');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

const appId = 'GettingStartedPortlet';

export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    Vue.createApp({
      template: `<exo-getting-started v-cacheable id="${appId}"></exo-getting-started>`,
      i18n,
      vuetify,
    }, appElement, 'Getting Started');
  });
}
