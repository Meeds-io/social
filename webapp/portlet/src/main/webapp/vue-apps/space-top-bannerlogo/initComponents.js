import ExoSpaceLogoBanner from './components/ExoSpaceLogoBanner.vue';
import SpacePopoverActionComponents from './components/SpacePopoverActionComponents.vue';
import ExoSpaceHostsDrawer from './components/ExoSpaceHostsDrawer.vue';


const components = {
  'exo-space-logo-banner': ExoSpaceLogoBanner,
  'space-popover-action-component': SpacePopoverActionComponents,
  'exo-space-hosts-drawer': ExoSpaceHostsDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
