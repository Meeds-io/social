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

Vue.use(Vuetify);
Vue.use(VueEllipsis);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.PeopleListApplication-${lang}.json`;

const appId = 'peopleListApplication';
const cacheId = `${appId}_${eXo.env.portal.spaceId}`;

export function init(filter, isManager, isExternalFeatureEnabled) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    new Vue({
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<space-members
                  v-cacheable="{cacheId: '${cacheId}'}"
                  id="${appId}"
                  :is-manager="${isManager}"
                  :is-external-feature-enabled="${isExternalFeatureEnabled}"
                  filter="${filter || 'member'}"
                  space-id="${eXo.env.portal.spaceId}"
                  class="singlePageApplication" />`,
      i18n,
      vuetify,
    }).$mount(appElement);
  });
}
