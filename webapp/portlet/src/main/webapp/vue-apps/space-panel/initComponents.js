import SpaceNavigationItem from './components/SpaceNavigationItem.vue';
import SpacePanelHamburgerNavigation from './components/SpacePanelHamburgerNavigation.vue';

const components = {
  'space-navigation-item': SpaceNavigationItem,
  'space-panel-hamburger-navigation': SpacePanelHamburgerNavigation
};

for (const key in components) {
  Vue.component(key, components[key]);
}

if (extensionRegistry) {
  extensionRegistry.registerExtension(
    'exo-hamburger-menu-navigation',
    'exo-hamburger-menu-navigation-items', {
      id: 'HamburgerMenuNavigationSpacePanel',
      priority: 30,
      secondLevel: true,
      vueComponent: SpaceNavigationItem,
    },
  );
}
