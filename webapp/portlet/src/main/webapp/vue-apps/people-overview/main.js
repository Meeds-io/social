import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('PeopleOverview');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.PeopleOverview-${lang}.json`;

const appId = 'PeopleOverview';

export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    Vue.createApp({
      template: `<people-overview id="${appId}" />`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'People Overview');
  });
}
