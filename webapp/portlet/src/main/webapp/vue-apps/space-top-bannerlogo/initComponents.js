import ExoSpaceLogoBanner from './components/ExoSpaceLogoBanner.vue';
import SpacePopoverActionComponents from './components/SpacePopoverActionComponents.vue';
import SpaceHostsDrawer from './components/SpaceHostsDrawer.vue';


const components = {
  'exo-space-logo-banner': ExoSpaceLogoBanner,
  'space-popover-action-component': SpacePopoverActionComponents,
  'space-hosts-drawer': SpaceHostsDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
