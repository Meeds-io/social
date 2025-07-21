/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import './initComponents.js';
import './initWebComponents.js';

import * as userService from './js/UserService.js';
import * as spaceService from './js/SpaceService.js';
import * as userStateService from './js/UserStateService.js';
import * as suggesterService from './js/SuggesterService.js';
import * as uploadService from './js/UploadService.js';
import * as identityService from './js/IdentityService.js';
import * as dateUtil from './js/DateUtil.js';
import * as settingService from './js/SettingService.js';
import * as featureService from './js/FeatureService.js';
import * as activityService from './js/ActivityService.js';
import * as favoriteService from './js/FavoriteService.js';
import * as observerService from './js/ObserverService.js';
import * as tagService from './js/TagService.js';
import * as socialWebSocket from './js/WebSocket.js';
import {spacesConstants} from './js/spacesConstants.js';
import * as utils from './js/Utils.js';
import * as brandingService from './js/brandingService.js';
import * as navigationService from '../common/js/NavigationService.js';
import * as profileSettingsService from '../common/js/ProfileSettingsService.js';
import * as profileLabelService from '../common/js/ProfileLabelService.js';
import * as siteService from './js/SiteService.js';
import * as navigationUtils from './js/NavigationUtils.js';
import * as spaceTemplateService from './js/SpaceTemplateService.js';
import * as registrationService from './js/RegistrationService';
import * as databindService from './js/DatabindService.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('CommonComponents');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

Vuetify.prototype.preset = eXo.env.portal.vuetifyPreset;
Vue.prototype.vuetifyOptions = new Vuetify(eXo.env.portal.vuetifyPreset);

Vue.use(Vuetify);

window.Object.defineProperty(Vue.prototype, '$siteService', {
  value: siteService,
});

window.Object.defineProperty(Vue.prototype, '$userService', {
  value: userService,
});
window.Object.defineProperty(Vue.prototype, '$userStateService', {
  value: userStateService,
});
window.Object.defineProperty(Vue.prototype, '$spaceService', {
  value: spaceService,
});
window.Object.defineProperty(Vue.prototype, '$suggesterService', {
  value: suggesterService,
});
window.Object.defineProperty(Vue.prototype, '$uploadService', {
  value: uploadService,
});
window.Object.defineProperty(Vue.prototype, '$identityService', {
  value: identityService,
});
window.Object.defineProperty(Vue.prototype, '$dateUtil', {
  value: dateUtil,
});
window.Object.defineProperty(Vue.prototype, '$settingService', {
  value: settingService,
});
window.Object.defineProperty(Vue.prototype, '$featureService', {
  value: featureService,
});
window.Object.defineProperty(Vue.prototype, '$activityService', {
  value: activityService,
});
window.Object.defineProperty(Vue.prototype, '$favoriteService', {
  value: favoriteService,
});
window.Object.defineProperty(Vue.prototype, '$observerService', {
  value: observerService,
});
window.Object.defineProperty(Vue.prototype, '$tagService', {
  value: tagService,
});
window.Object.defineProperty(Vue.prototype, '$socialWebSocket', {
  value: socialWebSocket,
});
window.Object.defineProperty(Vue.prototype, '$utils', {
  value: utils,
});
window.Object.defineProperty(Vue.prototype, '$spacesConstants', {
  value: spacesConstants,
});
window.Object.defineProperty(Vue.prototype, '$brandingService', {
  value: brandingService,
});
window.Object.defineProperty(Vue.prototype, '$profileSettingsService', {
  value: profileSettingsService,
});
window.Object.defineProperty(Vue.prototype, '$profileLabelService', {
  value: profileLabelService,
});
window.Object.defineProperty(Vue.prototype, '$navigationService', {
  value: navigationService,
});
window.Object.defineProperty(Vue.prototype, '$navigationUtils', {
  value: navigationUtils,
});
window.Object.defineProperty(Vue.prototype, '$spaceTemplateService', {
  value: spaceTemplateService,
});
window.Object.defineProperty(Vue.prototype, '$registrationService', {
  value: registrationService,
});
window.Object.defineProperty(Vue.prototype, '$databindService', {
  value: databindService,
});

if (eXo.env.portal.userIdentityId) {
  window.Object.defineProperty(Vue.prototype, '$currentUserIdentity', {
    value: {
      id: eXo.env.portal.userIdentityId,
      username: eXo.env.portal.userName,
    },
  });
  identityService.getIdentityById(eXo.env.portal.userIdentityId)
    .then(identity => {
      if (identity) {
        Object.assign(Vue.prototype.$currentUserIdentity, identity);
      }
    });
}

const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';


if (!window.drawersOverlayInitialized) {
  const urls = [
    `/social/i18n/locale.portlet.Portlets?lang=${lang}`,
    `/social/i18n/locale.social.Webui?lang=${lang}`,
    `/social/i18n/locale.commons.Commons?lang=${lang}`,
    `/social/i18n/locale.portlet.social.UserPopup?lang=${lang}`,
    `/social/i18n/locale.portlet.social.SpacesListApplication?lang=${lang}`,
    `/social/i18n/locale.portal?lang=${lang}`,
  ];
  window.drawersOverlayInitialized = true;
  exoi18n.loadLanguageAsync(lang, urls)
    .then(i18n => init(i18n));
}

export function init(i18n) {
  const parentElement = document.querySelector('#drawers-overlay');
  if (!document.querySelector('#drawers-overlay')) {
    initSnackbar(i18n);
    return;
  }
  if ((document.readyState === 'interactive' && document.querySelector('#drawers-overlay')) || document.readyState === 'complete') {
    new Vue({
      template: '<drawers-overlay id="drawers-overlay" />',
      vuetify: Vue.prototype.vuetifyOptions,
      i18n,
    }).$mount(parentElement);
    parentElement.id = null;
    initSnackbar(i18n);
  } else {
    window.setTimeout(() => init(i18n), 50);
  }
}

export function initSnackbar(i18n) {
  let parentNotificationsElement = document.querySelector('#vuetify-apps') || document.querySelector('#body-end-container');
  let alertNotificationsElement = parentNotificationsElement?.querySelector('#alert-notifications');
  if (!alertNotificationsElement) {
    if (!parentNotificationsElement) {
      parentNotificationsElement = document.createElement('div');
      document.body.appendChild(parentNotificationsElement);
    }
    alertNotificationsElement = document.createElement('div');
    alertNotificationsElement.id = 'alert-notifications';
    alertNotificationsElement.class = 'v-application v-application--is-ltr transparent theme--light';
    parentNotificationsElement.appendChild(alertNotificationsElement);
    parentNotificationsElement.classList.add('VuetifyApp');
    new Vue({
      template: '<v-app id="alert-notifications"><alert-notifications /></v-app>',
      vuetify: Vue.prototype.vuetifyOptions,
      i18n,
    }).$mount(alertNotificationsElement);
  }
}