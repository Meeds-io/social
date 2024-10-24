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
import '../space-form/initComponents.js';

import SpacesList from './components/SpacesList.vue';
import SpacesToolbar from './components/SpacesToolbar.vue';
import SpacesCardList from './components/SpacesCardList.vue';
import SpacesListFilterDrawer from './components/SpacesListFilterDrawer.vue';
import SpaceCard from './components/SpaceCard.vue';
import SpaceCardButton from './components/SpaceCardButton.vue';
import SpaceCardMenu from './components/SpaceCardMenu.vue';
import SpaceCardMenuItem from './components/SpaceCardMenuItem.vue';
import SpaceCardUnreadBadge from './components/SpaceCardUnreadBadge.vue';
import SpaceFavoriteAction from './components/SpaceFavoriteAction.vue';
import SpacePendingButton from './components/SpacePendingButton.vue';
import SpacePendingDrawer from './components/SpacePendingDrawer.vue';
import SpaceRoleListItem from './components/SpaceRoleListItem.vue';
import SpaceRoleList from './components/SpaceRoleList.vue';

const components = {
  'spaces-list': SpacesList,
  'spaces-toolbar': SpacesToolbar,
  'spaces-card-list': SpacesCardList,
  'spaces-pending-drawer': SpacePendingDrawer,
  'spaces-role-list-item': SpaceRoleListItem,
  'spaces-role-list': SpaceRoleList,
  'spaces-list-filter-drawer': SpacesListFilterDrawer,
  'space-card': SpaceCard,
  'space-card-button': SpaceCardButton,
  'space-card-menu': SpaceCardMenu,
  'space-card-menu-item': SpaceCardMenuItem,
  'space-card-unread-badge': SpaceCardUnreadBadge,
  'space-favorite-action': SpaceFavoriteAction,
  'space-pending-button': SpacePendingButton,
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
