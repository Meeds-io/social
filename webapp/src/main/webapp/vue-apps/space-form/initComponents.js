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
import SpaceFormAccess from './components/SpaceFormAccess.vue';
import SpaceFormAvatar from './components/SpaceFormAvatar.vue';
import SpaceFormBanner from './components/SpaceFormBanner.vue';
import SpaceFormDrawer from './components/SpaceFormDrawer.vue';
import SpaceFormInvitation from './components/SpaceFormInvitation.vue';

import SpaceFormInviteEmailDrawer from './components/SpaceFormInviteEmailDrawer.vue';
import SpaceFormInviteUsersDrawer from './components/SpaceFormInviteUsersDrawer.vue';

import SpaceFormInviteUsersInput from './components/SpaceFormInviteUsersInput.vue';

import SpaceFormInviteEmailListItem from './components/SpaceFormInviteEmailListItem.vue';
import SpaceFormInviteUserListItem from './components/SpaceFormInviteUserListItem.vue';

const components = {
  'space-form-access': SpaceFormAccess,
  'space-form-avatar': SpaceFormAvatar,
  'space-form-banner': SpaceFormBanner,
  'space-form-drawer': SpaceFormDrawer,
  'space-form-invitation': SpaceFormInvitation,
  'space-form-invite-email-drawer': SpaceFormInviteEmailDrawer,
  'space-form-invite-users-drawer': SpaceFormInviteUsersDrawer,
  'space-form-invite-email-list-item': SpaceFormInviteEmailListItem,
  'space-form-invite-user-list-item': SpaceFormInviteUserListItem,
  'space-form-invite-users-input': SpaceFormInviteUsersInput,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
