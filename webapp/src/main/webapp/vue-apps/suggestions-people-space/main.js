import './initComponents.js';

// getting language of the PLF
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

// should expose the locale ressources as REST API
const url = `/social/i18n/locale.portlet.social.SuggestionsPortlet?lang=${lang}`;


// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ExoSuggestionsPeopleAndSpace');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

const appId = 'SuggestionsPeopleAndSpace';

export function init(suggestionsType) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    Vue.createApp({
      template: `<exo-suggestions-people-and-space id="${appId}" suggestionsType="${suggestionsType || 'all'}"></exo-suggestions-people-and-space>`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, `Suggestions ${suggestionsType}`);
  });
}
