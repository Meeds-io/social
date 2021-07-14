import ExoUserHamburgerNavigation from './components/ExoUserHamburgerNavigation.vue';

const components = {
  'exo-user-hamburger-navigation': ExoUserHamburgerNavigation,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

if (extensionRegistry) {
  extensionRegistry.registerExtension(
    'exo-hamburger-menu-navigation',
    'exo-hamburger-menu-navigation-items', {
      id: 'HamburgerMenuNavigationUser',
      priority: 50,
      vueComponent: ExoUserHamburgerNavigation,
    },
  );
}