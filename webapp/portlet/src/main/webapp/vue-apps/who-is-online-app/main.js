import './components/initComponents.js';

// getting language of the PLF
const lang = `${Vue.prototype.$spacesConstants.LANG}`;

// should expose the locale ressources as REST API
const url = `${Vue.prototype.$spacesConstants.PORTAL}/${Vue.prototype.$spacesConstants.PORTAL_REST}/i18n/bundle/locale.portlet.whoisonline.whoisonline-${lang}.json`;

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('WhoIsOnLinePortlet');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

const appId = 'OnlinePortlet';
const appName = 'Who is Online';

// getting locale ressources
export function init() {
  document.dispatchEvent(new CustomEvent('vue-app-loading-start', {detail: {
    appName: appName,
    time: Date.now(),
  }}));
  exoi18n.loadLanguageAsync(lang, url)
    .then(() => {
      const onlineUsers = JSON.parse(document.getElementById('whoIsOnlineDefaultValue').value);
      if (onlineUsers && onlineUsers.length) {
        const avatars = JSON.parse(document.getElementById('whoIsOnlineAvatarsDefaultValue').value);
        onlineUsers.forEach(user => {
          user.avatar = avatars[user.userId];
        });
      }
      Vue.createApp({
        data: {
          onlineUsers,
        },
        template: `<exo-who-is-online id="${appId}"></exo-who-is-online>`,
        vuetify: Vue.prototype.vuetifyOptions,
        i18n: exoi18n.i18n,
      }, `#${appId}`, appName);
    });
}