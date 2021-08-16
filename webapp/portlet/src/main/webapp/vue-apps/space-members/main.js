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
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.PeopleListApplication-${lang}.json`;

const appId = 'peopleListApplication';

export function init(filter, isManager, isExternalFeatureEnabled) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    Vue.createApp({
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<space-members
                  id="${appId}"
                  :is-manager="${isManager}"
                  :is-external-feature-enabled="${isExternalFeatureEnabled}"
                  filter="${filter || 'member'}"
                  space-id="${eXo.env.portal.spaceId}"
                  class="singlePageApplication" />`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'Space Members');
  });
}
