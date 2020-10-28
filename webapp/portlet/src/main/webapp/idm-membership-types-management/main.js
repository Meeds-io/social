import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('MembershipTypesManagement');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

Vue.use(Vuetify);
const vuetify = new Vuetify({
  dark: true,
  iconfont: '',
});

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

const appId = 'MembershipTypesManagement';

//getting language of the PLF
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

//should expose the locale ressources as REST API 
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`;

export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    // init Vue app when locale ressources are ready
    new Vue({
      template: `<membership-types-management v-cacheable id="${appId}" />`,
      vuetify,
      i18n
    }).$mount(appElement);
  });
}
