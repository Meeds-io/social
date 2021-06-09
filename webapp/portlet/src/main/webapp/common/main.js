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
  `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.commons.Commons-${lang}.json`,
];

exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
  new Vue({i18n});
});