import ExoSpaceLogoBanner from './components/ExoSpaceLogoBanner.vue';
import SpacePopoverActionComponents from './components/SpacePopoverActionComponents.vue';
import SpaceHostsDrawer from './components/SpaceHostsDrawer.vue';
import ExoSpaceFavoriteAction from '../spaces-list/components/ExoSpaceFavoriteAction.vue';


const components = {
  'exo-space-logo-banner': ExoSpaceLogoBanner,
  'space-popover-action-component': SpacePopoverActionComponents,
  'space-hosts-drawer': SpaceHostsDrawer,
  'exo-space-favorite-action': ExoSpaceFavoriteAction
};

for (const key in components) {
  Vue.component(key, components[key]);
}
