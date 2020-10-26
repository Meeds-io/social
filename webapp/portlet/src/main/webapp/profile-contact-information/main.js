import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ProfileContactInformation');
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

const appId = 'ProfileContactInformation';
const cacheId = `${appId}_${eXo.env.portal.profileOwnerIdentityId}`;

//should expose the locale ressources as REST API 
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.${appId}-${lang}.json`;

export function init(user, uploadLimit) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    new Vue({
      data: () => ({
        user: user,
      }),
      template: `<profile-contact-information v-cacheable="{cacheId: '${cacheId}'}" id="${appId}" :user="user" :upload-limit="${uploadLimit}" />`,
      i18n,
      vuetify,
    }).$mount(appElement);
  });
}
