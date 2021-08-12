import './initComponents.js';

import * as notificationService from './js/NotificationService.js';

if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('TopBarNotification');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

window.Object.defineProperty(Vue.prototype, '$notificationService', {
  value: notificationService,
});

notificationService.initCometd();

document.addEventListener('cometdNotifEvent', updateBadge);
export function updateBadge(event) {
  if (event && event.detail) {
    const badge = event.detail.data.numberOnBadge;
    const badgeWrapper = document.querySelector('#NotificationPopoverPortlet .v-badge__wrapper');
    if (badge === 0) {
      badgeWrapper.classList.add('hidden');
    } else {
      badgeWrapper.classList.remove('hidden');
    }
    badgeWrapper.querySelector('span').innerText = badge;
  }
}

//getting locale ressources
export function init() {
  document.removeEventListener('cometdNotifEvent', updateBadge);

  const appId = 'NotificationPopoverPortlet';
  const lang = eXo.env.portal.language;
  const url = `${Vue.prototype.$spacesConstants.PORTAL}/${Vue.prototype.$spacesConstants.PORTAL_REST}/i18n/bundle/locale.portlet.Portlets-${lang}.json`;
  exoi18n.loadLanguageAsync(lang, url)
    .then(() => {
      // init Vue app when locale ressources are ready
      Vue.createApp({
        template: `<exo-top-bar-notification id="${appId}"></exo-top-bar-notification>`,
        vuetify: Vue.prototype.vuetifyOptions,
        i18n: exoi18n.i18n,
      }, `#${appId}`, 'Topbar Notifications');
    });
}