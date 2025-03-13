import './initComponents.js';

import * as userService from './js/UserService.js';
import * as spaceService from './js/SpaceService.js';
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
import { spacesConstants } from './js/spacesConstants.js';
import * as utils from './js/Utils.js';
import * as brandingService from './js/brandingService.js';
import * as navigationService from '../common/js/NavigationService.js';
import * as profileSettingsService from '../common/js/ProfileSettingsService.js';
import * as profileLabelService from '../common/js/ProfileLabelService.js';
import * as siteService from './js/SiteService.js';
import * as navigationUtils from './js/NavigationUtils.js';
import * as spaceTemplateService from './js/SpaceTemplateService.js';
import * as registrationService from './js/RegistrationService.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('CommonComponents');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

window.Object.defineProperty(eXo, '$siteService', {
  value: siteService,
});

window.Object.defineProperty(eXo, '$userService', {
  value: userService,
});
window.Object.defineProperty(eXo, '$spaceService', {
  value: spaceService,
});
window.Object.defineProperty(eXo, '$suggesterService', {
  value: suggesterService,
});
window.Object.defineProperty(eXo, '$uploadService', {
  value: uploadService,
});
window.Object.defineProperty(eXo, '$identityService', {
  value: identityService,
});
window.Object.defineProperty(eXo, '$dateUtil', {
  value: dateUtil,
});
window.Object.defineProperty(eXo, '$settingService', {
  value: settingService,
});
window.Object.defineProperty(eXo, '$featureService', {
  value: featureService,
});
window.Object.defineProperty(eXo, '$activityService', {
  value: activityService,
});
window.Object.defineProperty(eXo, '$favoriteService', {
  value: favoriteService,
});
window.Object.defineProperty(eXo, '$observerService', {
  value: observerService,
});
window.Object.defineProperty(eXo, '$tagService', {
  value: tagService,
});
window.Object.defineProperty(eXo, '$socialWebSocket', {
  value: socialWebSocket,
});
window.Object.defineProperty(eXo, '$utils', {
  value: utils,
});
window.Object.defineProperty(eXo, '$spacesConstants', {
  value: spacesConstants,
});
window.Object.defineProperty(eXo, '$brandingService', {
  value: brandingService,
});
window.Object.defineProperty(eXo, '$profileSettingsService', {
  value: profileSettingsService,
});
window.Object.defineProperty(eXo, '$profileLabelService', {
  value: profileLabelService,
});
window.Object.defineProperty(eXo, '$navigationService', {
  value: navigationService,
});
window.Object.defineProperty(eXo, '$navigationUtils', {
  value: navigationUtils,
});
window.Object.defineProperty(eXo, '$spaceTemplateService', {
  value: spaceTemplateService,
});
window.Object.defineProperty(eXo, '$registrationService', {
  value: registrationService,
});

if (eXo.env.portal.userIdentityId) {
  window.Object.defineProperty(eXo, '$currentUserIdentity', {
    value: {
      id: eXo.env.portal.userIdentityId,
      username: eXo.env.portal.userName,
    },
  });
  identityService.getIdentityById(eXo.env.portal.userIdentityId)
    .then(identity => {
      if (identity) {
        Object.assign(eXo.$currentUserIdentity, identity);
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

export function init (i18n) {
  const parentElement = document.querySelector('#drawers-overlay');
  if (!document.querySelector('#drawers-overlay')) {
    initSnackbar(i18n);
    return;
  }
  if ((document.readyState === 'interactive' && parentElement) || document.readyState === 'complete') {
    Vue.createApp({
      template: '<drawers-overlay id="drawers-overlay" />',
      vuetify: eXo.vuetify,
      i18n,
    }, '#drawers-overlay', 'Drawers Overlay');
    parentElement.id = null;
    initSnackbar(i18n);
  } else {
    window.setTimeout(() => init(i18n), 50);
  }
}

export function initSnackbar (i18n) {
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
    Vue.createApp({
      template: '<v-app id="alert-notifications"><alert-notifications /></v-app>',
      vuetify: eXo.vuetify,
      i18n,
    }, '#alert-notifications', 'Alert Notifications');
  }
}