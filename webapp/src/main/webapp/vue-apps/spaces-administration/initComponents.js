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
import SpacesAdministration from './components/SpacesAdministration.vue';

import SpacesAdministrationList from './components/main/SpacesAdministrationList.vue';
import SpacesAdministrationToolbar from './components/main/SpacesAdministrationToolbar.vue';

import SpacesAdministrationItem from './components/item/SpacesAdministrationItem.vue';
import SpacesAdministrationItemMenu from './components/item/SpacesAdministrationItemMenu.vue';

import SpacesAdministrationPermissions from './components/form/SpacesAdministrationPermissions.vue';

import SpacesAdministrationDeleteMenuItem from './components/menu-action/SpacesAdministrationDeleteMenuItem.vue';
import SpacesAdministrationSettingsMenuItem from './components/menu-action/SpacesAdministrationSettingsMenuItem.vue';

import SpacesAdministrationBindingReportItem from './components/binding-report/SpacesAdministrationBindingReportItem.vue';
import SpacesAdministrationBindingReportList from './components/binding-report/SpacesAdministrationBindingReportList.vue';

import SpacesAdministrationManagersDrawer from './components/drawer/SpacesAdministrationManagersDrawer.vue';
import SpacesAdministrationSyncMembersDrawer from './components/drawer/SpacesAdministrationSyncMembersDrawer.vue';
import SpacesAdministrationSyncReportsDrawer from './components/drawer/SpacesAdministrationSyncReportsDrawer.vue';

const components = {
  'spaces-administration': SpacesAdministration,
  'spaces-administration-toolbar': SpacesAdministrationToolbar,

  'spaces-administration-list': SpacesAdministrationList,
  'spaces-administration-item': SpacesAdministrationItem,
  'spaces-administration-item-menu': SpacesAdministrationItemMenu,

  'spaces-administration-binding-report-list': SpacesAdministrationBindingReportList,
  'spaces-administration-binding-report-item': SpacesAdministrationBindingReportItem,

  'spaces-administration-permissions': SpacesAdministrationPermissions,

  'spaces-administration-delete-menu-item': SpacesAdministrationDeleteMenuItem,
  'spaces-administration-settings-menu-item': SpacesAdministrationSettingsMenuItem,

  'spaces-administration-managers-drawer': SpacesAdministrationManagersDrawer,
  'spaces-administration-sync-members-drawer': SpacesAdministrationSyncMembersDrawer,
  'spaces-administration-sync-reports-drawer': SpacesAdministrationSyncReportsDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}