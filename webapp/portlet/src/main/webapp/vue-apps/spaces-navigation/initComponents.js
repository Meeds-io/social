import ExoSpacesHamburgerNavigation from './components/ExoSpacesHamburgerNavigation.vue';
import ExoRecentSpacesHamburgerNavigation from './components/ExoRecentSpacesHamburgerNavigation.vue';
import ExoSpacesNavigationContent from './components/ExoSpacesNavigationContent.vue';
import SpaceNavigationItem from './components/SpaceNavigationItem.vue';
import SpacePanelHamburgerNavigation from './components/SpacePanelHamburgerNavigation.vue';
import SpaceHostsDrawer from './components/SpaceHostsDrawer.vue';

const components = {
  'exo-spaces-hamburger-menu-navigation': ExoSpacesHamburgerNavigation,
  'exo-recent-spaces-hamburger-menu-navigation': ExoRecentSpacesHamburgerNavigation,
  'exo-spaces-navigation-content': ExoSpacesNavigationContent,
  'space-navigation-item': SpaceNavigationItem,
  'space-panel-hamburger-navigation': SpacePanelHamburgerNavigation,
  'space-hosts-drawer': SpaceHostsDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

if (extensionRegistry) {
  extensionRegistry.registerExtension(
    'exo-hamburger-menu-navigation',
    'exo-hamburger-menu-navigation-items', {
      id: 'HamburgerMenuNavigationSpaces',
      priority: 20,
      secondLevel: true,
      vueComponent: ExoSpacesHamburgerNavigation,
    },
  );
}
