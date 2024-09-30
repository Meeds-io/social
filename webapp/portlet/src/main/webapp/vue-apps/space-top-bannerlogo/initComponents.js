import SpaceLogoBanner from './components/SpaceLogoBanner.vue';
import SpacePopoverActionComponents from './components/SpacePopoverActionComponents.vue';
import SpaceHostsDrawer from './components/SpaceHostsDrawer.vue';
import ExoSpaceFavoriteAction from '../spaces-list/components/ExoSpaceFavoriteAction.vue';


const components = {
  'space-logo-banner': SpaceLogoBanner,
  'space-popover-action-component': SpacePopoverActionComponents,
  'space-hosts-drawer': SpaceHostsDrawer,
  'exo-space-favorite-action': ExoSpaceFavoriteAction
};

for (const key in components) {
  Vue.component(key, components[key]);
}
