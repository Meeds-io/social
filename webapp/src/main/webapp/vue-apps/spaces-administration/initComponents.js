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
import SpacesAdministrationPermissionsLabel from './components/form/SpacesAdministrationPermissionsLabel.vue';
import SpacesAdministrationTemplateCharacteristic from './components/form/SpacesAdministrationTemplateCharacteristic.vue';

import SpacesAdministrationDeleteMenuItem from './components/menu-action/SpacesAdministrationDeleteMenuItem.vue';
import SpacesAdministrationSettingsMenuItem from './components/menu-action/SpacesAdministrationSettingsMenuItem.vue';
import SpacesAdministrationSynMembersMenuItem from './components/menu-action/SpacesAdministrationSynMembersMenuItem.vue';
import SpacesAdministrationPermissionsMenuItem from './components/menu-action/SpacesAdministrationPermissionsMenuItem.vue';
import SpacesAdministrationApplyTemplateMenuItem from './components/menu-action/SpacesAdministrationApplyTemplateMenuItem.vue';
import SpacesAdministrationSaveAsTemplateMenuItem from './components/menu-action/SpacesAdministrationSaveAsTemplateMenuItem.vue';

import SpacesAdministrationProcessingAlert from './components/bulk-action/SpacesAdministrationProcessingAlert.vue';
import SpacesAdministrationBulkApplyTemplate from './components/bulk-action/SpacesAdministrationBulkApplyTemplate.vue';
import SpacesAdministrationBulkSyncMembers from './components/bulk-action/SpacesAdministrationBulkSyncMembers.vue';
import SpacesAdministrationBulkDelete from './components/bulk-action/SpacesAdministrationBulkDelete.vue';

import SpacesAdministrationBindingReportItem from './components/binding-report/SpacesAdministrationBindingReportItem.vue';
import SpacesAdministrationBindingReportList from './components/binding-report/SpacesAdministrationBindingReportList.vue';

import SpacesAdministrationManagersDrawer from './components/drawer/SpacesAdministrationManagersDrawer.vue';
import SpacesAdministrationSyncMembersDrawer from './components/drawer/SpacesAdministrationSyncMembersDrawer.vue';
import SpacesAdministrationSyncReportsDrawer from './components/drawer/SpacesAdministrationSyncReportsDrawer.vue';
import SpacesAdministrationPermissionsDrawer from './components/drawer/SpacesAdministrationPermissionsDrawer.vue';
import SpacesAdministrationApplyTemplateDrawer from './components/drawer/SpacesAdministrationApplyTemplateDrawer.vue';

const components = {
  'spaces-administration': SpacesAdministration,
  'spaces-administration-toolbar': SpacesAdministrationToolbar,

  'spaces-administration-list': SpacesAdministrationList,
  'spaces-administration-item': SpacesAdministrationItem,
  'spaces-administration-item-menu': SpacesAdministrationItemMenu,

  'spaces-administration-binding-report-list': SpacesAdministrationBindingReportList,
  'spaces-administration-binding-report-item': SpacesAdministrationBindingReportItem,

  'spaces-administration-permissions': SpacesAdministrationPermissions,
  'spaces-administration-permissions-label': SpacesAdministrationPermissionsLabel,
  'spaces-administration-template-characteristic': SpacesAdministrationTemplateCharacteristic,

  'spaces-administration-delete-menu-item': SpacesAdministrationDeleteMenuItem,
  'spaces-administration-settings-menu-item': SpacesAdministrationSettingsMenuItem,
  'spaces-administration-sync-members-menu-item': SpacesAdministrationSynMembersMenuItem,
  'spaces-administration-permissions-menu-item': SpacesAdministrationPermissionsMenuItem,
  'spaces-administration-apply-template-menu-item': SpacesAdministrationApplyTemplateMenuItem,
  'spaces-administration-save-as-template-menu-item': SpacesAdministrationSaveAsTemplateMenuItem,

  'spaces-administration-processing-alert': SpacesAdministrationProcessingAlert,
  'spaces-administration-bulk-apply-template': SpacesAdministrationBulkApplyTemplate,
  'spaces-administration-bulk-sync-members': SpacesAdministrationBulkSyncMembers,
  'spaces-administration-bulk-delete': SpacesAdministrationBulkDelete,

  'spaces-administration-managers-drawer': SpacesAdministrationManagersDrawer,
  'spaces-administration-sync-members-drawer': SpacesAdministrationSyncMembersDrawer,
  'spaces-administration-sync-reports-drawer': SpacesAdministrationSyncReportsDrawer,
  'spaces-administration-permissions-drawer': SpacesAdministrationPermissionsDrawer,
  'spaces-administration-apply-template-drawer': SpacesAdministrationApplyTemplateDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}