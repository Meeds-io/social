import './initComponents.js';

// getting language of the PLF
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

// should expose the locale ressources as REST API
const url = `${Vue.prototype.$spacesConstants.PORTAL}/${Vue.prototype.$spacesConstants.PORTAL_REST}/i18n/bundle/locale.portlet.social.SuggestionsPortlet-${lang}.json`;


// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ExoSuggestionsPeopleAndSpace');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

const appId = 'SuggestionsPeopleAndSpace';

export function init(suggestionsType) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    const cacheId = `${appId}_${suggestionsType || ''}`;

    new Vue({
      template: `<exo-suggestions-people-and-space v-cacheable="{cacheId: '${cacheId}'}" id="${appId}" suggestionsType="${suggestionsType || 'all'}"></exo-suggestions-people-and-space>`,
      i18n,
      vuetify,
    }).$mount(appElement);
  });
}
