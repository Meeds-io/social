/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import HamburgerMenuNavigation from './components/HamburgerMenuNavigation.vue';
import HamburgerMenuNavigationButton from './components/HamburgerMenuNavigationButton.vue';
import HamburgerMenuParentDrawer from './components/HamburgerMenuParentDrawer.vue';
import HamburgerMenuParentSticky from './components/HamburgerMenuParentSticky.vue';
import HamburgerMenuNavigationFirstLevel from './components/HamburgerMenuNavigationFirstLevel.vue';
import HamburgerMenuNavigationSecondLevel from './components/HamburgerMenuNavigationSecondLevel.vue';
import HamburgerMenuNavigationThirdLevel from './components/HamburgerMenuNavigationThirdLevel.vue';
import ProfileHamburgerNavigation from './components/profile/ProfileHamburgerNavigation.vue';
import RecentSpacesHamburgerNavigation from './components/recent-spaces/RecentSpacesHamburgerNavigation.vue';
import SpaceNavigationItem from './components/recent-spaces/SpaceNavigationItem.vue';
import SpacePanelHamburgerNavigation from './components/recent-spaces/SpacePanelHamburgerNavigation.vue';
import SpacePanelHamburgerNavigationItem from './components/recent-spaces/SpacePanelHamburgerNavigationItem.vue';
import SpacesHamburgerNavigation from './components/recent-spaces/SpacesHamburgerNavigation.vue';
import SpacesNavigationContent from './components/recent-spaces/SpacesNavigationContent.vue';
import SpacesNavigationEmpty from './components/recent-spaces/SpacesNavigationEmpty.vue';
import SiteHamburgerNavigation from './components/site/SiteHamburgerNavigation.vue';
import UserHamburgerNavigation from './components/user/UserHamburgerNavigation.vue';
import SitesHamburger from './components/site/SitesHamburger.vue';
import SiteHamburgerItem from './components/site/SiteHamburgerItem.vue';

const components = {
  'hamburger-menu-navigation': HamburgerMenuNavigation,
  'hamburger-menu-navigation-button': HamburgerMenuNavigationButton,
  'hamburger-menu-parent-drawer': HamburgerMenuParentDrawer,
  'hamburger-menu-parent-menu': HamburgerMenuParentSticky,
  'hamburger-menu-navigation-first-level': HamburgerMenuNavigationFirstLevel,
  'hamburger-menu-navigation-second-level': HamburgerMenuNavigationSecondLevel,
  'hamburger-menu-navigation-third-level': HamburgerMenuNavigationThirdLevel,
  'profile-hamburger-navigation': ProfileHamburgerNavigation,
  'spaces-hamburger-navigation': SpacesHamburgerNavigation,
  'recent-spaces-hamburger-navigation': RecentSpacesHamburgerNavigation,
  'space-navigation-item': SpaceNavigationItem,
  'space-panel-hamburger-navigation': SpacePanelHamburgerNavigation,
  'space-panel-hamburger-navigation-item': SpacePanelHamburgerNavigationItem,
  'spaces-navigation-content': SpacesNavigationContent,
  'spaces-navigation-empty': SpacesNavigationEmpty,
  'site-hamburger-navigation': SiteHamburgerNavigation,
  'user-hamburger-navigation': UserHamburgerNavigation,
  'sites-hamburger': SitesHamburger,
  'site-hamburger-item': SiteHamburgerItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
