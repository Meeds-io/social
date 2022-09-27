import SpaceMenu from './components/SpaceMenu.vue';
import SpaceMenuItem from './components/SpaceMenuItem.vue';

const components = {
  'space-menu': SpaceMenu,
  'space-menu-item': SpaceMenuItem
};

for (const key in components) {
  Vue.component(key, components[key]);
}
