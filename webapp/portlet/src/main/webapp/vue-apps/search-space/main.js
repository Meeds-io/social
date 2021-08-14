import './initComponents.js';

export function formatSearchResult(result) {
  return result && result.spaces;
}

$('.VuetifyApp .v-application').first().append('<div id="SpaceSearchDrawers" />');

const appId = 'SpaceSearchDrawers';

const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

const urls = [`${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`];
exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
  new Vue({
    template: '<space-search-drawers />',
    vuetify: Vue.prototype.vuetifyOptions,
    i18n,
  }).$mount(`#${appId}`);
});