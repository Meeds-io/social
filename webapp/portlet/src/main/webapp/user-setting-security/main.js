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

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

Vue.use(Vuetify);
Vue.use(VueEllipsis);
const vuetify = new Vuetify({
  dark: true,
  iconfont: '',
});

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.UserSettings-${lang}.json`;

const appId = 'UserSettingSecurity';

export function init(ssoEnabled) {
  if (ssoEnabled) {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
  } else {
    exoi18n.loadLanguageAsync(lang, url).then(i18n => {
      // init Vue app when locale ressources are ready
      new Vue({
        template: `<user-setting-security id="${appId}" />`,
        i18n,
        vuetify,
      }).$mount(`#${appId}`);
    });
  }
}
