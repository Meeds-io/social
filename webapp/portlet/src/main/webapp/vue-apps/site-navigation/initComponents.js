import ExoSiteHamburgerNavigation from './components/ExoSiteHamburgerNavigation.vue';

const components = {
  'exo-site-hamburger-navigation': ExoSiteHamburgerNavigation,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

if (extensionRegistry) {
  extensionRegistry.registerExtension(
    'exo-hamburger-menu-navigation',
    'exo-hamburger-menu-navigation-items', {
      id: 'HamburgerMenuNavigationSite',
      priority: 10,
      vueComponent: ExoSiteHamburgerNavigation,
    },
  );
}