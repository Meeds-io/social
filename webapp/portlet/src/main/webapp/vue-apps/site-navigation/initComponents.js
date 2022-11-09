import ExoSiteHamburgerNavigation from './components/ExoSiteHamburgerNavigation.vue';
import ExoSiteHamburgerNavigationLegacy from './components/legacy/ExoSiteHamburgerNavigation.vue';

const components = {};

if (eXo.env.portal.leftMenuEnabled) {
  components['exo-site-hamburger-navigation'] = ExoSiteHamburgerNavigation;
} else {
  components['exo-site-hamburger-navigation'] = ExoSiteHamburgerNavigationLegacy;
}


for (const key in components) {
  Vue.component(key, components[key]);
}

if (extensionRegistry) {
  extensionRegistry.registerExtension(
    'exo-hamburger-menu-navigation',
    'exo-hamburger-menu-navigation-items', {
      id: 'HamburgerMenuNavigationSite',
      priority: 10,
      vueComponent: components['exo-site-hamburger-navigation'],
    },
  );
}