import './initComponents.js';
// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('UserSettingSecurity');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

Vue.use(Vuetify);
Vue.use(VueEllipsis);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.UserSettings-${lang}.json`;

const appId = 'UserSettingSecurity';

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    new Vue({
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<user-setting-security id="${appId}" v-cacheable />`,
      i18n,
      vuetify,
    }).$mount(appElement);
  });
}
