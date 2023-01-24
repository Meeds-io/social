// eslint-disable-next-line max-len
import ExoAdministrationHamburgerNavigation from './components/ExoAdministrationHamburgerNavigation.vue';
// eslint-disable-next-line max-len
import ExoAdministrationMenuItem from './components/ExoAdministrationMenuItem.vue';
import ExoAdministrationNavigations from './components/ExoAdministrationNavigations.vue';

const components = {
  // eslint-disable-next-line max-len
  'exo-administration-menu-item': ExoAdministrationMenuItem,
  'exo-administration-navigations': ExoAdministrationNavigations,
  'exo-administration-hamburger-menu-navigation': ExoAdministrationHamburgerNavigation,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

if (extensionRegistry) {
  extensionRegistry.registerExtension(
    'exo-hamburger-menu-navigation',
    'exo-hamburger-menu-navigation-items', {
      id: 'HamburgerMenuNavigationAdministration',
      priority: 40,
      secondLevel: true,
      vueComponent: components['exo-administration-hamburger-menu-navigation'],
    },
  );
}
