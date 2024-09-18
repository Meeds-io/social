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

import SpaceSettingAvatar from './components/form/SpaceSettingAvatar.vue';
import SpaceSettingBanner from './components/form/SpaceSettingBanner.vue';

import SpaceSettingRolesTable from './components/section/roles/SpaceSettingRolesTable.vue';
import SpaceSettingRolesTableItem from './components/section/roles/SpaceSettingRolesTableItem.vue';

import SpaceSettingsRolesList from './components/section/roles/SpaceSettingsRolesList.vue';
import SpaceSettingsRolesListItem from './components/section/roles/SpaceSettingsRolesListItem.vue';

const components = {
  'space-settings': SpaceSettings,
  'space-setting-overview': SpaceSettingOverview,
  'space-setting-roles': SpaceSettingRoles,
  'space-setting-access': SpaceSettingAccess,

  'space-setting-overview-section': SpaceSettingOverviewSection,

  'space-setting-roles-section': SpaceSettingRolesSection,
  'space-setting-roles-table': SpaceSettingRolesTable,
  'space-setting-roles-table-item': SpaceSettingRolesTableItem,
  'space-setting-roles-list': SpaceSettingsRolesList,
  'space-setting-roles-list-item': SpaceSettingsRolesListItem,

  'space-setting-access-drawer': SpaceSettingAccessDrawer,
  'space-setting-redactor-drawer': SpaceSettingRedactorDrawer,

  'space-setting-avatar': SpaceSettingAvatar,
  'space-setting-banner': SpaceSettingBanner,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
