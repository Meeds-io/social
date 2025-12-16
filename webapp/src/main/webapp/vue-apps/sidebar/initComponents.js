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
import Sidebar from './components/Sidebar.vue';
import SidebarButton from './components/SidebarButton.vue';

import SidebarParentDrawer from './components/parent/SidebarParentDrawer.vue';
import SidebarParentSticky from './components/parent/SidebarParentSticky.vue';

import SidebarFirstLevel from './components/drawer/SidebarFirstLevel.vue';
import SidebarSecondLevel from './components/drawer/SidebarSecondLevel.vue';
import SidebarThirdLevel from './components/drawer/SidebarThirdLevel.vue';

import SpacesHamburgerNavigation from './components/space/SpacesHamburgerNavigation.vue';
import SpaceNavigationItem from './components/space/SpaceNavigationItem.vue';
import SpaceUnreadBadge from './components/space/SpaceUnreadBadge.vue';
import SpacePanelHamburgerNavigation from './components/space/SpacePanelHamburgerNavigation.vue';
import SpacesNavigationContent from './components/space/SpacesNavigationContent.vue';
import SpacesNavigationEmpty from './components/space/SpacesNavigationEmpty.vue';
import SpaceHamburgerActionMenu from './components/space/SpaceHamburgerActionMenu.vue';

import SidebarList from './components/list/SidebarList.vue';
import SidebarListItem from './components/list/SidebarListItem.vue';
import SidebarListFooter from './components/list/SidebarListFooter.vue';
import SidebarListHeader from './components/list/SidebarListHeader.vue';
import SidebarListContent from './components/list/SidebarListContent.vue';
import SidebarListSubList from './components/list/SidebarListSubList.vue';

import SidebarHomeDialog from './components/dialog/SidebarHomeDialog.vue';
import SpaceCreationButton from '../spaces-list-components/components/common/SpaceCreationButton.vue';

const components = {
  'sidebar': Sidebar,
  'sidebar-button': SidebarButton,
  'sidebar-parent-drawer': SidebarParentDrawer,
  'sidebar-parent-menu': SidebarParentSticky,
  'sidebar-first-level': SidebarFirstLevel,
  'sidebar-second-level': SidebarSecondLevel,
  'sidebar-third-level': SidebarThirdLevel,
  'spaces-hamburger-navigation': SpacesHamburgerNavigation,
  'space-navigation-item': SpaceNavigationItem,
  'space-unread-badge': SpaceUnreadBadge,
  'space-panel-hamburger-navigation': SpacePanelHamburgerNavigation,
  'spaces-navigation-content': SpacesNavigationContent,
  'spaces-navigation-empty': SpacesNavigationEmpty,
  'space-hamburger-action-menu': SpaceHamburgerActionMenu,
  'space-creation-button': SpaceCreationButton,
  'sidebar-list': SidebarList,
  'sidebar-list-item': SidebarListItem,
  'sidebar-list-footer': SidebarListFooter,
  'sidebar-list-header': SidebarListHeader,
  'sidebar-list-content': SidebarListContent,
  'sidebar-list-sub-list': SidebarListSubList,
  'sidebar-home-dialog': SidebarHomeDialog,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
