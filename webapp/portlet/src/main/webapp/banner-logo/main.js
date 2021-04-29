import './initComponents.js';
import { spacesConstants } from '../js/spacesConstants.js';

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

// getting language of the PLF
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

// should expose the locale ressources as REST API
const url = `${spacesConstants.PORTAL}/${spacesConstants.PORTAL_REST}/i18n/bundle/locale.portlet.social.SpaceInfosPortlet-${lang}.json`;

Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

const appId = 'TopBarLogo';
export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    new Vue({
      template: `<banner-logo  id="${appId}"></banner-logo>`,
      vuetify,
      i18n
    }).$mount(appElement);
  });
}