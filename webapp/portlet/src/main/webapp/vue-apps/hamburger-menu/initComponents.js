import HamburgerMenuNavigation from './components/HamburgerMenuNavigation.vue';
import HamburgerMenuParentDrawer from './components/HamburgerMenuParentDrawer.vue';
import HamburgerMenuParentSticky from './components/HamburgerMenuParentSticky.vue';
import HamburgerMenuNavigationFirstLevel from './components/HamburgerMenuNavigationFirstLevel.vue';
import HamburgerMenuNavigationSecondLevel from './components/HamburgerMenuNavigationSecondLevel.vue';
import HamburgerMenuNavigationThirdLevel from './components/HamburgerMenuNavigationThirdLevel.vue';
import AdministrationHamburgerNavigation from './components/administration/AdministrationHamburgerNavigation.vue';
import AdministrationMenuItem from './components/administration/AdministrationMenuItem.vue';
import AdministrationNavigations from './components/administration/AdministrationNavigations.vue';
import ProfileHamburgerNavigation from './components/profile/ProfileHamburgerNavigation.vue';
import RecentSpacesHamburgerNavigation from './components/recent-spaces/RecentSpacesHamburgerNavigation.vue';
import SpaceNavigationItem from './components/recent-spaces/SpaceNavigationItem.vue';
import SpacePanelHamburgerNavigation from './components/recent-spaces/SpacePanelHamburgerNavigation.vue';
import SpacePanelHamburgerNavigationItem from './components/recent-spaces/SpacePanelHamburgerNavigationItem.vue';
import SpacesHamburgerNavigation from './components/recent-spaces/SpacesHamburgerNavigation.vue';
import SpacesNavigationContent from './components/recent-spaces/SpacesNavigationContent.vue';
import SiteHamburgerNavigation from './components/site/SiteHamburgerNavigation.vue';
import UserHamburgerNavigation from './components/user/UserHamburgerNavigation.vue';

const components = {
  'hamburger-menu-navigation': HamburgerMenuNavigation,
  'hamburger-menu-parent-drawer': HamburgerMenuParentDrawer,
  'hamburger-menu-parent-menu': HamburgerMenuParentSticky,
  'hamburger-menu-navigation-first-level': HamburgerMenuNavigationFirstLevel,
  'hamburger-menu-navigation-second-level': HamburgerMenuNavigationSecondLevel,
  'hamburger-menu-navigation-third-level': HamburgerMenuNavigationThirdLevel,
  'administration-hamburger-navigation': AdministrationHamburgerNavigation,
  'administration-menu-item': AdministrationMenuItem,
  'administration-navigations': AdministrationNavigations,
  'profile-hamburger-navigation': ProfileHamburgerNavigation,
  'spaces-hamburger-navigation': SpacesHamburgerNavigation,
  'recent-spaces-hamburger-navigation': RecentSpacesHamburgerNavigation,
  'space-navigation-item': SpaceNavigationItem,
  'space-panel-hamburger-navigation': SpacePanelHamburgerNavigation,
  'space-panel-hamburger-navigation-item': SpacePanelHamburgerNavigationItem,
  'spaces-navigation-content': SpacesNavigationContent,
  'site-hamburger-navigation': SiteHamburgerNavigation,
  'user-hamburger-navigation': UserHamburgerNavigation,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
