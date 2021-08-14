import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('ProfileAboutMe');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

const appId = 'ProfileAboutMe';
const cacheId = `${appId}_${eXo.env.portal.profileOwnerIdentityId}`;

//should expose the locale ressources as REST API 
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.ProfileAboutMe-${lang}.json`;

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
export function init(aboutMe) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    Vue.createApp({
      data: {
        aboutMe: aboutMe,
      },
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<profile-about-me v-cacheable="{cacheId: '${cacheId}'}" id="${appId}" :about-me="aboutMe" />`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, appElement, 'Profile About Me');
  });
}
