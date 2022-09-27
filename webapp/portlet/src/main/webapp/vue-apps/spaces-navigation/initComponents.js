import ExoSpacesHamburgerNavigation from './components/ExoSpacesHamburgerNavigation.vue';
import ExoRecentSpacesHamburgerNavigation from './components/ExoRecentSpacesHamburgerNavigation.vue';
import ExoSpacesNavigationContent from './components/ExoSpacesNavigationContent.vue';
import ExoSpaceNavigationItem from './components/ExoSpaceNavigationItem.vue';
import ExoSpacePanelHamburgerNavigation from './components/ExoSpacePanelHamburgerNavigation.vue';
import SpaceHostsDrawer from './components/SpaceHostsDrawer.vue';

const components = {
  'exo-spaces-hamburger-menu-navigation': ExoSpacesHamburgerNavigation,
  'exo-recent-spaces-hamburger-menu-navigation': ExoRecentSpacesHamburgerNavigation,
  'exo-spaces-navigation-content': ExoSpacesNavigationContent,
  'exo-space-navigation-item': ExoSpaceNavigationItem,
  'exo-space-panel-hamburger-navigation': ExoSpacePanelHamburgerNavigation,
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
