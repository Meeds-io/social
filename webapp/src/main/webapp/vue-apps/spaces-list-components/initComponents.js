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
import SpacesList from './components/SpacesList.vue';

import SpacesToolbar from './components/toolbar/SpacesToolbar.vue';
import SpacePendingButton from './components/toolbar/SpacePendingButton.vue';
import SpacesPublicAccessWarning from './components/toolbar/SpacesPublicAccessWarning.vue';

import SpacesCardList from './components/list/SpacesCardList.vue';

import SpacesListFilterDrawer from './components/drawer/SpacesListFilterDrawer.vue';
import SpacePendingDrawer from './components/drawer/SpacePendingDrawer.vue';
import SpacesListSettingsDrawer from './components/drawer/SpacesListSettingsDrawer.vue';

import SpaceCard from './components/space-card/SpaceCard.vue';
import SpaceCardMenu from './components/space-card/menu/SpaceCardMenu.vue';
import SpaceCardMenuItem from './components/space-card/menu/SpaceCardMenuItem.vue';
import SpaceRoleListItem from './components/space-card/menu/SpaceRoleListItem.vue';
import SpaceRoleList from './components/space-card/menu/SpaceRoleList.vue';

import SpaceCardButton from './components/common/SpaceCardButton.vue';
import SpaceCardUnreadBadge from './components/common/SpaceCardUnreadBadge.vue';
import SpaceFavoriteAction from './components/common/SpaceFavoriteAction.vue';
import SpacesFilterSettings from './components/list/SpacesFilterSettings.vue';
import SpaceCreationButton from './components/common/SpaceCreationButton.vue';

const components = {
  'spaces-list': SpacesList,
  'spaces-toolbar': SpacesToolbar,
  'spaces-card-list': SpacesCardList,
  'spaces-pending-drawer': SpacePendingDrawer,
  'spaces-role-list-item': SpaceRoleListItem,
  'spaces-role-list': SpaceRoleList,
  'spaces-list-filter-drawer': SpacesListFilterDrawer,
  'spaces-list-settings-drawer': SpacesListSettingsDrawer,
  'space-card': SpaceCard,
  'space-card-button': SpaceCardButton,
  'space-card-menu': SpaceCardMenu,
  'space-card-menu-item': SpaceCardMenuItem,
  'space-card-unread-badge': SpaceCardUnreadBadge,
  'space-favorite-action': SpaceFavoriteAction,
  'space-pending-button': SpacePendingButton,
  'spaces-public-access-warning': SpacesPublicAccessWarning,
  'spaces-filter-settings': SpacesFilterSettings,
  'space-creation-button': SpaceCreationButton
};

for (const key in components) {
  Vue.component(key, components[key]);
}

//get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpacesList');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}
