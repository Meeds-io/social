import './initComponents.js';

Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

const appId = 'SearchApplication';

//getting language of the PLF 
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

//should expose the locale ressources as REST API 
const urls = [`${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`];

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
export function init(connectors) {
  if (connectors && connectors.length) {
    connectors.forEach(connector => {
      if (connector.i18nBundle) {
        urls.push(`${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/${connector.i18nBundle}-${lang}.json`);
      }
    });
  }

  exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    new Vue({
      data: () => ({
        connectors: connectors,
      }),
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<search-application v-cacheable id="${appId}" :connectors="connectors" />`,
      vuetify,
      i18n
    }).$mount(appElement);
  });
}
