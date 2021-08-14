import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('HamburgerMenu');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

Vuetify.prototype.preset = eXo.env.portal.vuetifyPreset;

const lang = eXo && eXo.env && eXo.env.portal && eXo.env.portal.language || 'en';
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portal.HamburgerMenu-${lang}.json`;

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

export function init() {
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => {
      // init Vue app when locale ressources are ready
      Vue.createApp({
        mounted() {
          document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        },
        template: '<exo-hamburger-menu-navigation></exo-hamburger-menu-navigation>',
        i18n,
        vuetify: Vue.prototype.vuetifyOptions,
      }, '#HamburgerNavigationMenu', 'Hamburger Menu');
    });
}