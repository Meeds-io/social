import TopBarNavigationMenu from './components/TopBarNavigationMenu.vue';
import NavigationMenuItem from './components/NavigationMenuItem.vue';
import NavigationMenuSubItem from './components/NavigationMenuSubItem.vue';

import * as NavigationService from '../common/js/NavigationService.js';

const components = {
  'top-bar-navigation-menu': TopBarNavigationMenu,
  'navigation-menu-item': NavigationMenuItem,
  'navigation-menu-sub-item': NavigationMenuSubItem
};

for (const key in components) {
  Vue.component(key, components[key]);
}

if (!Vue.prototype.$navigationService) {
  window.Object.defineProperty(Vue.prototype, '$navigationService', {
    value: NavigationService,
  });
}