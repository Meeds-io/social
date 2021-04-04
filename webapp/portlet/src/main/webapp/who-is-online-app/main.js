import './components/initComponents.js';
import { spacesConstants } from '../js/spacesConstants.js';

// getting language of the PLF
const lang = `${spacesConstants.LANG}`;

// should expose the locale ressources as REST API
const url = `${spacesConstants.PORTAL}/${spacesConstants.PORTAL_REST}/i18n/bundle/locale.portlet.whoisonline.whoisonline-${lang}.json`;

Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('WhoIsOnLinePortlet');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

const appId = 'OnlinePortlet';

// getting locale ressources
export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    new Vue({
      template: `<exo-who-is-online v-cacheable id="${appId}"></exo-who-is-online>`,
      i18n,
      vuetify
    }).$mount(appElement);
  });
}