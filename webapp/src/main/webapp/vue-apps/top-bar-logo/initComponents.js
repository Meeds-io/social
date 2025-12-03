import TopBarLogo from './components/TopBarLogo.vue';
import CompanyName from './components/CompanyName.vue';
import SiteName from './components/SiteName.vue';

import SpaceFavoriteAction from '../spaces-list-components/components/common/SpaceFavoriteAction.vue';
import SpaceLogoBanner from './components/space/SpaceLogoBanner.vue';
import SpacePopoverActionComponents from './components/space/SpacePopoverActionComponents.vue';
import SpaceHostsDrawer from './components/space/SpaceHostsDrawer.vue';

const components = {
  'top-bar-logo': TopBarLogo,
  'top-bar-logo-company-name': CompanyName,
  'top-bar-logo-site-name': SiteName,
  'space-logo-banner': SpaceLogoBanner,
  'space-popover-action-component': SpacePopoverActionComponents,
  'space-hosts-drawer': SpaceHostsDrawer,
  'space-favorite-action': SpaceFavoriteAction,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
