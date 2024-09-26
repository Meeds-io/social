import './initComponents.js';

// getting language of the PLF 
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

// should expose the locale ressources as REST API 
const url = `/social-portlet/i18n/locale.portlet.social.SpaceInfosPortlet?lang=${lang}`;

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpaceInfos');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

const appId = 'spaceInfosApp';
const cacheId = `${appId}_${eXo.env.portal.spaceId}`;

// getting locale ressources
export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    Vue.createApp({
      template: `<space-infos v-cacheable="{cacheId: '${cacheId}'}" id="${appId}" />`,
      vuetify: Vue.prototype.vuetifyOptions,
      i18n,
    }, appElement, 'Space Info');
  });
}