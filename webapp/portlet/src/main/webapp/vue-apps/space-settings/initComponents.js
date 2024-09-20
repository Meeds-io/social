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
import SpaceSettings from './components/SpaceSettings.vue';

import SpaceSettingOverview from './components/main/SpaceSettingOverview.vue';
import SpaceSettingRoles from './components/main/SpaceSettingRoles.vue';
import SpaceSettingAccess from './components/main/SpaceSettingAccess.vue';

import SpaceSettingOverviewSection from './components/section/SpaceSettingOverviewSection.vue';
import SpaceSettingRolesSection from './components/section/SpaceSettingRolesSection.vue';

import SpaceSettingAccessDrawer from './components/drawer/SpaceSettingAccessDrawer.vue';
import SpaceSettingRedactorDrawer from './components/drawer/SpaceSettingRedactorDrawer.vue';
import SpaceSettingUsersListDrawer from './components/drawer/SpaceSettingUsersListDrawer.vue';
import SpaceSettingUsersSelectionDrawer from './components/drawer/SpaceSettingUsersSelectionDrawer.vue';
import SpaceSettingUsersInvitationDrawer from './components/drawer/SpaceSettingUsersInvitationDrawer.vue';
import SpaceSettingPendingManagementDrawer from './components/drawer/SpaceSettingPendingManagementDrawer.vue';
import SpaceSettingEmailInvitationDrawer from './components/drawer/SpaceSettingEmailInvitationDrawer.vue';

import SpaceSettingAvatar from './components/form/SpaceSettingAvatar.vue';
import SpaceSettingBanner from './components/form/SpaceSettingBanner.vue';
import SpaceSettingUsersListToolbar from './components/form/SpaceSettingUsersListToolbar.vue';
import SpaceSettingInviteButton from './components/form/SpaceSettingInviteButton.vue';

import SpaceSettingRolesTable from './components/section/roles/SpaceSettingRolesTable.vue';
import SpaceSettingRolesTableItem from './components/section/roles/SpaceSettingRolesTableItem.vue';

import SpaceSettingRolesList from './components/section/roles/SpaceSettingRolesList.vue';
import SpaceSettingRolesListItem from './components/section/roles/SpaceSettingRolesListItem.vue';
import SpaceSettingInvitationListItem from './components/section/roles/SpaceSettingInvitationListItem.vue';

const components = {
  'space-settings': SpaceSettings,
  'space-setting-overview': SpaceSettingOverview,
  'space-setting-roles': SpaceSettingRoles,
  'space-setting-access': SpaceSettingAccess,

  'space-setting-overview-section': SpaceSettingOverviewSection,

  'space-setting-roles-section': SpaceSettingRolesSection,
  'space-setting-roles-table': SpaceSettingRolesTable,
  'space-setting-roles-table-item': SpaceSettingRolesTableItem,
  'space-setting-roles-list': SpaceSettingRolesList,
  'space-setting-roles-list-item': SpaceSettingRolesListItem,
  'space-setting-invitation-list-item': SpaceSettingInvitationListItem,

  'space-setting-access-drawer': SpaceSettingAccessDrawer,
  'space-setting-redactor-drawer': SpaceSettingRedactorDrawer,
  'space-setting-users-list-drawer': SpaceSettingUsersListDrawer,
  'space-setting-users-selection-drawer': SpaceSettingUsersSelectionDrawer,
  'space-setting-users-invitation-drawer': SpaceSettingUsersInvitationDrawer,
  'space-setting-users-pending-management-drawer': SpaceSettingPendingManagementDrawer,
  'space-setting-email-pending-management-drawer': SpaceSettingEmailInvitationDrawer,

  'space-setting-avatar': SpaceSettingAvatar,
  'space-setting-banner': SpaceSettingBanner,
  'space-setting-users-list-toolbar': SpaceSettingUsersListToolbar,
  'space-setting-invite-button': SpaceSettingInviteButton,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
