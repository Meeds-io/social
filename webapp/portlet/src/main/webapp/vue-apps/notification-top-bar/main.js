/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

import './initComponents.js';
import './services.js';

if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('TopBarNotification');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

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

const appId = 'NotificationPopoverPortlet';
const lang = eXo.env.portal.language;
const urls = [
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.notification.template.Notification-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.commons.Commons-${lang}.json`,
];

//getting locale ressources
export function init() {
  document.removeEventListener('cometdNotifEvent', updateBadge);
  exoi18n.loadLanguageAsync(lang, urls)
    .then(() => {
      // init Vue app when locale ressources are ready
      Vue.createApp({
        data: {
          notificationExtensions: {},
        },
        template: `<top-bar-notification id="${appId}"></top-bar-notification>`,
        vuetify: Vue.prototype.vuetifyOptions,
        i18n: exoi18n.i18n,
        created() {
          document.addEventListener('extension-WebNotification-notification-content-extension-updated', this.refreshNotificationExtensions);
          this.refreshNotificationExtensions();
        },
        methods: {
          refreshNotificationExtensions() {
            const extensions = extensionRegistry.loadExtensions('WebNotification', 'notification-content-extension');
            extensions.forEach(extension => {
              if (extension.type) {
                this.$set(this.notificationExtensions, extension.type, extension);
              }
            });
          },
        },
      }, `#${appId}`, 'Topbar Notifications');
    })
    .finally(() => Vue.prototype.$utils.includeExtensions('NotificationPopoverExtension'));
}