import './initComponents.js';

Vue.use(Vuetify);
const vuetify = new Vuetify({
  dark: true,
  iconfont: '',
});

const appId = 'SearchApplication';

//getting language of the PLF 
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

//should expose the locale ressources as REST API 
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`;

export function init(connectors) {
  document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale ressources are ready
    new Vue({
      data: () => ({
        connectors: connectors,
      }),
      template: `<search-application id="${appId}" :connectors="connectors" />`,
      vuetify,
      i18n
    }).$mount(`#${appId}`);
  });
}
