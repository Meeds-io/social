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
import {spacesConstants} from './js/spacesConstants.js';
import * as utils from './js/Utils.js';

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

Vue.use(Vuetify);
Vue.use(VueEllipsis);

Vue.prototype.vuetifyOptions = new Vuetify(eXo.env.portal.vuetifyPreset);

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
window.Object.defineProperty(Vue.prototype, '$utils', {
  value: utils,
});
window.Object.defineProperty(Vue.prototype, '$spacesConstants', {
  value: spacesConstants,
});

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

const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

const urls = [
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Portlets-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.social.Webui-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.commons.Commons-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.UserPopup-${lang}.json`,
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.social.SpacesListApplication-${lang}.json`,
];

exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
  const parentElement = document.querySelector('#MiddleToolBarChildren');
  let drawersOverlayElement = parentElement.querySelector('#drawers-overlay');
  if (!drawersOverlayElement) {
    drawersOverlayElement = document.createElement('div');
    drawersOverlayElement.id = 'drawers-overlay';
    drawersOverlayElement.class = 'v-application v-application--is-ltr transparent theme--light';
    parentElement.appendChild(drawersOverlayElement);
    parentElement.classList.add('VuetifyApp');
  }

  new Vue({
    created() {
      this.$userPopupLabels = Vue.prototype.$userPopupLabels = {
        CancelRequest: this.$t('UserProfilePopup.label.CancelRequest'),
        Confirm: this.$t('UserProfilePopup.label.Confirm'),
        Connect: this.$t('UserProfilePopup.label.Connect'),
        Ignore: this.$t('UserProfilePopup.label.Ignore'),
        RemoveConnection: this.$t('UserProfilePopup.label.RemoveConnection'),
        StatusTitle: this.$t('UserProfilePopup.label.Loading'),
        External: this.$t('UserProfilePopup.label.External'),
        Disabled: this.$t('UserProfilePopup.label.Disabled'),
        Loading: this.$t('UserProfilePopup.label.Loading'),
      };
      this.$spacePopupLabels = Vue.prototype.$spacePopupLabels = {
        CancelRequest: this.$t('UserProfilePopup.label.CancelRequest'),
        Confirm: this.$t('UserProfilePopup.label.Confirm'),
        Connect: this.$t('UserProfilePopup.label.Connect'),
        Ignore: this.$t('UserProfilePopup.label.Ignore'),
        RemoveConnection: this.$t('UserProfilePopup.label.RemoveConnection'),
        StatusTitle: this.$t('UserProfilePopup.label.Loading'),
        External: this.$t('UserProfilePopup.label.External'),
        join: this.$t('spacesList.button.join'),
        leave: this.$t('spacesList.button.leave'),
        members: this.$t('spacesList.label.SpaceMembers'),
      };
    },
    template: '<drawers-overlay id="drawers-overlay" />',
    vuetify: Vue.prototype.vuetifyOptions,
    i18n,
  }).$mount(drawersOverlayElement);
});
