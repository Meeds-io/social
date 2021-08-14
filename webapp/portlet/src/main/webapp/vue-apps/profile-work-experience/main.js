import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ProfileWorkExperience');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

const appId = 'ProfileWorkExperience';
const cacheId = `${appId}_${eXo.env.portal.profileOwnerIdentityId}`;

//should expose the locale ressources as REST API 
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.ProfileWorkExperience-${lang}.json`;

export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    Vue.createApp({
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<profile-work-experiences v-cacheable="{cacheId: '${cacheId}'}" id="${appId}" />`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, appElement, 'Profile Work Experience');
  });
}
