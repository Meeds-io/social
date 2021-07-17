import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('UserSettingNotifications');
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
const urls = [
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.notification.UserNotificationPortlet-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.UserSettings-${lang}.json`
];

const appId = 'UserSettingNotifications';

export function init(settings) {
  exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    new Vue({
      data: () => ({
        settings: settings,
      }),
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<user-setting-notifications v-cacheable id="${appId}" :languages="settings && settings.languages" :timezones="settings && settings.timezones" />`,
      i18n,
      vuetify,
    }).$mount(appElement);
  });
}
