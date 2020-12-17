import './components/initComponents.js';
import { spacesConstants } from '../js/spacesConstants.js';
import * as spacesAdministrationDirectives from './spacesAdministrationDirectives.js';

Vue.use(Vuetify);

const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

// getting language of the PLF 
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

// should expose the locale ressources as REST API 
const url = `${spacesConstants.PORTAL}/${spacesConstants.PORTAL_REST}/i18n/bundle/locale.portlet.social.SpacesAdministrationPortlet-${lang}.json`;

Vue.directive('exo-tooltip', spacesAdministrationDirectives.tooltip);

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpacesAdministration');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

const appId = 'spacesAdministration';

// getting locale ressources
export function init(applicationsByCategory) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    new Vue({
      data: () => ({
        applicationsByCategory: applicationsByCategory,
      }),
      template: `<exo-spaces-administration-spaces v-cacheable id="${appId}" :applications-by-category="applicationsByCategory"></exo-spaces-administration-spaces>`,
      i18n,
      vuetify,
    }).$mount(appElement);
  });
}
