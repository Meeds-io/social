import './initComponents.js';
import './services.js';

// getting language of the PLF 
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

// should expose the locale ressources as REST API 
const urls = [
  `/social-portlet/i18n/locale.portlet.social.SpacesAdministrationPortlet?lang=${lang}`,
  `/social-portlet/i18n/locale.portal.webui?lang=${lang}`,
];

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
  exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    Vue.createApp({
      data: {
        applicationsByCategory: applicationsByCategory,
      },
      template: `<exo-spaces-administration-spaces v-cacheable id="${appId}" :applications-by-category="applicationsByCategory"></exo-spaces-administration-spaces>`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, appElement, 'Spaces Administration');
  });
}