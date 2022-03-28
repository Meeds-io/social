import './initComponents.js';
import './extensions.js';

if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('TopBarFavorites');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

//getting locale ressources
export function init() {

  const appId = 'favoritesListPortlet';
  const lang = eXo.env.portal.language;
  const url = `${Vue.prototype.$spacesConstants.PORTAL}/${Vue.prototype.$spacesConstants.PORTAL_REST}/i18n/bundle/locale.portlet.Portlets-${lang}.json`;
  exoi18n.loadLanguageAsync(lang, url)
    .then(() => {
      // init Vue app when locale ressources are ready
      Vue.createApp({
        template: `<exo-top-bar-favorites id="${appId}"></exo-top-bar-favorites>`,
        vuetify: Vue.prototype.vuetifyOptions,
        i18n: exoi18n.i18n,
      }, `#${appId}`, 'Topbar Favorites');
    });
}