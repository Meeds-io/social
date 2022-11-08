import ExoSpacesHamburgerNavigation from './components/ExoSpacesHamburgerNavigation.vue';
import ExoRecentSpacesHamburgerNavigation from './components/ExoRecentSpacesHamburgerNavigation.vue';
import ExoSpacesNavigationContent from './components/ExoSpacesNavigationContent.vue';
import SpaceNavigationItem from './components/SpaceNavigationItem.vue';
import SpacePanelHamburgerNavigation from './components/SpacePanelHamburgerNavigation.vue';
import LegacyExoSpacesHamburgerNavigation from './components/legacy/ExoSpacesHamburgerNavigation.vue';
import LegacyExoRecentSpacesHamburgerNavigation from './components/legacy/ExoRecentSpacesHamburgerNavigation.vue';
import LegacyExoSpacesNavigationContent from './components/legacy/ExoSpacesNavigationContent.vue';

const components = {
  'space-navigation-item': SpaceNavigationItem,
  'space-panel-hamburger-navigation': SpacePanelHamburgerNavigation
};

if (eXo.env.portal.leftMenuEnabled) {
  components['exo-spaces-hamburger-menu-navigation'] = ExoSpacesHamburgerNavigation;
  components['exo-recent-spaces-hamburger-menu-navigation'] = ExoRecentSpacesHamburgerNavigation;
  components['exo-spaces-navigation-content'] = ExoSpacesNavigationContent;
} else {
  components['exo-spaces-hamburger-menu-navigation'] = LegacyExoSpacesHamburgerNavigation;
  components['exo-recent-spaces-hamburger-menu-navigation'] = LegacyExoRecentSpacesHamburgerNavigation;
  components['exo-spaces-navigation-content'] = LegacyExoSpacesNavigationContent;
}

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
      vueComponent: components['exo-spaces-hamburger-menu-navigation'],
    },
  );
}
