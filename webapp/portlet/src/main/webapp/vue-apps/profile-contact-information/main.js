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

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

const appId = 'ProfileContactInformation';
const cacheId = `${appId}_${eXo.env.portal.profileOwnerIdentityId}`;

//should expose the locale ressources as REST API 
const urls = [
  `/social-portlet/i18n/locale.portlet.social.ProfileContactInformation?lang=${lang}`,
  `/social-portlet/i18n/locale.portlet.social.ComplementaryFilter?lang=${lang}`,
  `/social-portlet/i18n/locale.portlet.Portlets?lang=${lang}`
];

export function init(uploadLimit, imTypes) {
  exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    Vue.createApp({
      data: () => ({
        imTypes,
      }),
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<profile-contact-information v-cacheable="{cacheId: '${cacheId}'}" id="${appId}" :upload-limit="${uploadLimit}" />`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, appElement, 'Profile Contact Information');
  });
}
