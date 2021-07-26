document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

Vue.use(Vuetify);
Vue.use(VueEllipsis);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const url = `${Vue.prototype.$spacesConstants.PORTAL}/${Vue.prototype.$spacesConstants.PORTAL_REST}/i18n/bundle/locale.portlet.social.SpacesListApplication-${lang}.json`;

const appId = 'spacesListApplication';

export function init(filter, canCreateSpace) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    // init Vue app when locale ressources are ready
    new Vue({
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<exo-spaces-list v-cacheable id="${appId}" filter="${filter || 'all'}" :can-create-space="${canCreateSpace}"></exo-spaces-list>`,
      i18n,
      vuetify,
    }).$mount(appElement);
  });
}