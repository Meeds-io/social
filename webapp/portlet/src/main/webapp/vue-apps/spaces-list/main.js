document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const url = `/social-portlet/i18n/locale.portlet.social.SpacesListApplication?lang=${lang}`;

const appId = 'spacesListApplication';

export function init(filter, canCreateSpace) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale ressources are ready
    Vue.createApp({
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<exo-spaces-list id="${appId}" filter="${filter || 'all'}" :can-create-space="${canCreateSpace}"></exo-spaces-list>`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'Spaces List');
  });
}