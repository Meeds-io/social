import './initComponents.js';
import './services.js';

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

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API 
const urls = [
  `/social/i18n/locale.portlet.UserNotificationPortlet?lang=${lang}`,
  `/social/i18n/locale.portlet.social.UserSettings?lang=${lang}`
];

const appId = 'UserSettingNotifications';

export function init(settings) {
  exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
    Vue.createApp({
      data: {
        settings: settings,
        autoOpen: window.location.hash === '#notifications',
      },
      created() {
        this.$utils.includeExtensions('UserSettingsNotificationsExtension');
        // Same loading as the notifications drawer: the core notification extensions
        // module first, then every add-on module named *NotificationExtension
        const includeNotificationExtensions = () => this.$utils.includeExtensions('NotificationExtension');
        window.require(['SHARED/notificationExtensions'], includeNotificationExtensions, includeNotificationExtensions);
      },
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<user-setting-notifications id="${appId}" />`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'User Settings Notifications');
  });
}
