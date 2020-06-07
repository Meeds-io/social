import './initComponents.js';
import { spacesConstants } from '../js/spacesConstants.js';

// getting language of the PLF 
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

// should expose the locale ressources as REST API 
const url = `${spacesConstants.PORTAL}/${spacesConstants.PORTAL_REST}/i18n/bundle/locale.portlet.social.GettingStartedPortlet-${lang}.json`;


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
const vuetify = new Vuetify({
  dark: true,
  iconfont: '',
});

export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    new Vue({
      template: '<exo-getting-started></exo-getting-started>',
      i18n,
      vuetify,
    }).$mount('#GettingStarted');
  });
}
