import HamburgerMenuNavigation from './components/ExoHamburgerMenuNavigation.vue';

const components = {
  // eslint-disable-next-line max-len
  'ExoHamburgerMenuNavigation': HamburgerMenuNavigation,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
