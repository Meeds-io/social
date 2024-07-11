import './initComponents.js';

export function formatSearchResult(result) {
  return result && result.spaces;
}

$('.VuetifyApp .v-application').first().append('<div id="SpaceSearchDrawers" />');

const appId = 'SpaceSearchDrawers';

const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

const urls = [`/social-portlet/i18n/locale.portlet.Portlets?lang=${lang}`];
exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
  Vue.createApp({
    template: '<space-search-drawers />',
    vuetify: Vue.prototype.vuetifyOptions,
    i18n,
  }, `#${appId}`, 'Search Space Drawers');
});