//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.PeopleListApplication-${lang}.json`;

const appId = 'peopleListApplication';

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

export function init(filter) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    Vue.createApp({
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<people-list id="${appId}" filter="${filter || 'all'}"></people-list>`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'People List');
  });
}