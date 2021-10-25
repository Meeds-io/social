import './initComponents.js';

const lang = eXo && eXo.env && eXo.env.portal && eXo.env.portal.language || 'en';
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.UserSettings-${lang}.json` ;

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

const appId = 'UserSettingsMenu';

export function init() {
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => {
      const appElement = document.createElement('div');
      appElement.id = appId;
      // init Vue app when locale ressources are ready
      Vue.createApp({
        mounted() {
          document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        },
        template: '<user-settings-menu></user-settings-menu>',
        i18n,
        vuetify: Vue.prototype.vuetifyOptions,
      }, appElement, 'User Settings Menu');
    });
}