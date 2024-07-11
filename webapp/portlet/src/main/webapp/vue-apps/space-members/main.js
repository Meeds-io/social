import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpaceMembers');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const url = `/social-portlet/i18n/locale.portlet.social.PeopleListApplication?lang=${lang}`;

const appId = 'spaceMembersApplication';

export function init(filter, isManager, isExternalFeatureEnabled) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    if (!filter?.length && window.location.hash.replace('#', '').length) {
      filter = window.location.hash.replace('#', '');
    }
    Vue.createApp({
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<space-members
                  id="${appId}"
                  :is-manager="${isManager}"
                  :is-external-feature-enabled="${isExternalFeatureEnabled}"
                  filter="${filter || 'member'}"
                  space-id="${eXo.env.portal.spaceId}" />`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'Space Members');
  });
}
