/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import ProfileSettings from './components/ProfileSettings.vue';
import ProfileSettingsHeader from './components/ProfileSettingsHeader.vue';
import ProfileSettingsTable from './components/ProfileSettingsTable.vue';
import ProfileSettingsActionMenu from './components/menu/ProfileSettingsActionMenu.vue';
import SettingsActionsCell from './components/cells/SettingsActionsCell.vue';
import ProfileSettingFormDrawer from './components/drawers/ProfileSettingFormDrawer.vue';
import ProfilePropertyLabels from './components/drawers/ProfilePropertyLabels.vue';
import PropertyLabel from './components/drawers/PropertyLabel.vue';

const components = {
  'profile-settings': ProfileSettings,
  'profile-settings-header': ProfileSettingsHeader,
  'profile-settings-table': ProfileSettingsTable,
  'profile-settings-action-menu': ProfileSettingsActionMenu,
  'profile-settings-actions-cell': SettingsActionsCell,
  'profile-setting-form-drawer': ProfileSettingFormDrawer,
  'profile-property-labels': ProfilePropertyLabels,
  'property-label': PropertyLabel,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
