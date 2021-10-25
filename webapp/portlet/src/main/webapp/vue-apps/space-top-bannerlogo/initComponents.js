import ExoSpaceLogoBanner from './components/ExoSpaceLogoBanner.vue';
import SpacePopoverActionComponents from './components/SpacePopoverActionComponents.vue';

const components = {
  'exo-space-logo-banner': ExoSpaceLogoBanner,
  'space-popover-action-component': SpacePopoverActionComponents
};

for (const key in components) {
  Vue.component(key, components[key]);
}
