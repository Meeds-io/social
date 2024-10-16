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

const appId = 'favoritesListPortlet';
const lang = eXo.env.portal.language;
const url = `/social-portlet/i18n/locale.portlet.Portlets?lang=${lang}`;

//getting locale ressources
export function init() {
  exoi18n.loadLanguageAsync(lang, url)
    .then(() => {
      // init Vue app when locale ressources are ready
      Vue.createApp({
        template: `<top-bar-favorites id="${appId}" />`,
        vuetify: Vue.prototype.vuetifyOptions,
        i18n: exoi18n.i18n,
      }, `#${appId}`, 'Topbar Favorites');
    })
    .finally(() => Vue.prototype.$utils.includeExtensions('FavoriteDrawerExtension'));
}