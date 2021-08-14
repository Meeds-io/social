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

const appId = 'SpaceHeader';
const cacheId = `${appId}_${eXo.env.portal.spaceId}`;

//getting language of the PLF 
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

//should expose the locale ressources as REST API 
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`;

export function init(settings, bannerUrl, maxUploadSize, isAdmin) {
  document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    Vue.createApp({
      data: {
        navigations: settings && settings.navigations,
        selectedNavigationUri: settings && settings.selectedNavigationUri,
        bannerUrl: bannerUrl,
      },
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<space-header v-cacheable="{cacheId: '${cacheId}'}" id="${appId}" :navigations="navigations" :selected-navigation-uri="selectedNavigationUri" :banner-url="bannerUrl" :max-upload-size="${maxUploadSize}" :admin="${isAdmin}" />`,
      vuetify: Vue.prototype.vuetifyOptions,
      i18n,
    }, appElement, 'Space Header');
  });
}
