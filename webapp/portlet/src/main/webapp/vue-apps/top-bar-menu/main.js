import './initComponents.js';

// get overridden components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('TopBarNavigationMenu');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`;

Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

const appId = 'topBarMenu';

export function init() {
  document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale resources are ready
    Vue.createApp({
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<top-bar-navigation-menu id="${appId}" />`,
      vuetify,
      i18n
    }, `#${appId}`, 'TopBar Navigation Menu');
  });
}