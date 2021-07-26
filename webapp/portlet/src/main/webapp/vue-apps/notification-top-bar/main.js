import './initComponents.js';

//getting language of the PLF 
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const url = `${Vue.prototype.$spacesConstants.PORTAL}/${Vue.prototype.$spacesConstants.PORTAL_REST}/i18n/bundle/locale.portlet.Portlets-${lang}.json`;

if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('TopBarNotification');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

const appId = 'NotificationPopoverPortlet';

//getting locale ressources
export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    // init Vue app when locale ressources are ready
    new Vue({
      template: `<exo-top-bar-notification v-cacheable id="${appId}"></exo-top-bar-notification>`,
      i18n,
      vuetify,
    }).$mount(appElement);
  });
}