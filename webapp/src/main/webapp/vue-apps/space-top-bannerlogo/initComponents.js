import SpaceLogoBanner from './components/SpaceLogoBanner.vue';
import SpacePopoverActionComponents from './components/SpacePopoverActionComponents.vue';
import SpaceHostsDrawer from './components/SpaceHostsDrawer.vue';
import SpaceFavoriteAction from '../spaces-list/components/SpaceFavoriteAction.vue';


const components = {
  'space-logo-banner': SpaceLogoBanner,
  'space-popover-action-component': SpacePopoverActionComponents,
  'space-hosts-drawer': SpaceHostsDrawer,
  'space-favorite-action': SpaceFavoriteAction
};

for (const key in components) {
  Vue.component(key, components[key]);
}
