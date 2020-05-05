import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpaceHeader');
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

const appId = 'SpaceHeader';

//getting language of the PLF 
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

//should expose the locale ressources as REST API 
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`;

export function init(settings, bannerUrl, maxUploadSize, isAdmin) {
  document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale ressources are ready
    new Vue({
      data: () => ({
        navigations: settings && settings.navigations,
        selectedNavigationUri: settings && settings.selectedNavigationUri,
        bannerUrl: bannerUrl,
      }),
      template: `<space-header id="${appId}" :navigations="navigations" :selected-navigation-uri="selectedNavigationUri" :banner-url="bannerUrl" :max-upload-size="${maxUploadSize}" :admin="${isAdmin}" />`,
      vuetify,
      i18n
    }).$mount(`#${appId}`);
  });
}
