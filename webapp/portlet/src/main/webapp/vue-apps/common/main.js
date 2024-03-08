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
import {spacesConstants} from './js/spacesConstants.js';
import * as utils from './js/Utils.js';
import * as brandingService from './js/brandingService.js';
import * as navigationService from '../common/js/NavigationService.js';
import * as profileSettingsService from '../common/js/ProfileSettingsService.js';
import * as profileLabelService from '../common/js/ProfileLabelService.js';
import * as siteService from './js/SiteService.js';
import * as applicationRegistryService from './js/ApplicationRegistryService.js';

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
window.Object.defineProperty(Vue.prototype, '$applicationRegistryService', {
  value: applicationRegistryService,
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

const urls = [
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.social.Webui-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.commons.Commons-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.UserPopup-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.SpacesListApplication-${lang}.json`,
];

if (!window.drawersOverlayInitialized) {
  window.drawersOverlayInitialized = true;
  exoi18n.loadLanguageAsync(lang, urls)
    .then(i18n => init(i18n));
}

export function init(i18n) {
  if ((document.readyState === 'interactive' && document.querySelector('#drawers-overlay')) || document.readyState === 'complete') {
    if (document.querySelector('#drawers-overlay')) {
      new Vue({
        template: '<drawers-overlay id="drawers-overlay" />',
        vuetify: Vue.prototype.vuetifyOptions,
        i18n,
      }).$mount('#drawers-overlay');
    } else if (!document.querySelector('#UIPortalApplication')) { // Needed for anonymous pages (login, register ...)
      const parentDrawersOverlayElement = document.querySelector('#MiddleToolBarChildren') || document.body;
      let drawersOverlayElement = parentDrawersOverlayElement.querySelector('#drawers-overlay');
      if (!drawersOverlayElement) {
        drawersOverlayElement = document.createElement('div');
        drawersOverlayElement.id = 'drawers-overlay';
        drawersOverlayElement.class = 'v-application v-application--is-ltr transparent theme--light';
        parentDrawersOverlayElement.appendChild(drawersOverlayElement);
        parentDrawersOverlayElement.classList.add('VuetifyApp');
        new Vue({
          template: '<drawers-overlay id="drawers-overlay" />',
          vuetify: Vue.prototype.vuetifyOptions,
          i18n,
        }).$mount(drawersOverlayElement);
      }
    }
    let parentNotificationsElement = document.querySelector('#bottom-all-container');
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
      }).$mount(alertNotificationsElement);
    }
  } else {
    window.setTimeout(() => init(i18n), 50);
  }
}