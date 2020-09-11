import './initComponents.js';

import * as userService from './js/UserService.js';
import * as spaceService from './js/SpaceService.js';
import * as suggesterService from './js/SuggesterService.js';
import * as uploadService from './js/UploadService.js';
import * as identityService from './js/IdentityService.js';
import * as dateUtil from './js/DateUtil.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('CommonComponents');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

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
