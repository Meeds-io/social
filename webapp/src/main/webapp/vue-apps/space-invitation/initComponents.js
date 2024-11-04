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
import SpaceInviteButtonsGroup from './components/SpaceInviteButtonsGroup.vue';

import SpaceInviteButton from './components/form/SpaceInviteButton.vue';

import SpaceInvitePendingDrawer from './components/drawer/SpaceInvitePendingDrawer.vue';
import SpaceInviteInputUsersDrawer from './components/drawer/SpaceInviteInputUsersDrawer.vue';
import SpaceInviteInputEmailDrawer from './components/drawer/SpaceInviteInputEmailDrawer.vue';

import SpaceSettingRoleList from './components/list/SpaceSettingRoleList.vue';
import SpaceSettingRoleListItem from './components/list/SpaceSettingRoleListItem.vue';
import SpaceInviteEmailListItem from './components/list/SpaceInviteEmailListItem.vue';

const components = {
  'space-invite-buttons-group': SpaceInviteButtonsGroup,
  'space-invite-button': SpaceInviteButton,

  'space-invite-pending-drawer': SpaceInvitePendingDrawer,
  'space-invite-input-users-drawer': SpaceInviteInputUsersDrawer,
  'space-invite-input-email-drawer': SpaceInviteInputEmailDrawer,

  'space-setting-role-list': SpaceSettingRoleList,
  'space-setting-role-list-item': SpaceSettingRoleListItem,
  'space-invite-email-list-item': SpaceInviteEmailListItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
