import SpaceMenu from './components/SpaceMenu.vue';

const components = {
  'space-menu': SpaceMenu,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
