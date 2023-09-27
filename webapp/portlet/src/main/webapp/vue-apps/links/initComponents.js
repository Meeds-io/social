/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

import LinksApp from './components/LinksApp.vue';

import LinkSettingsDrawer from './components/settings/LinkSettingsDrawer.vue';
import LinkAdvancedSettingsDrawer from './components/settings/LinkAdvancedSettingsDrawer.vue';
import LinkFormDrawer from './components/settings/LinkFormDrawer.vue';
import LinkDisplayPreview from './components/settings/LinkDisplayPreview.vue';
import LinkInput from './components/settings/LinkInput.vue';
import LinkIconInput from './components/settings/LinkIconInput.vue';

import LinksList from './components/view/LinksList.vue';
import LinksItem from './components/view/LinksItem.vue';
import LinksItemIcon from './components/view/LinksItemIcon.vue';

const components = {
  'links-app': LinksApp,
  'links-settings-drawer': LinkSettingsDrawer,
  'links-advanced-settings-drawer': LinkAdvancedSettingsDrawer,
  'links-form-drawer': LinkFormDrawer,
  'links-display-preview': LinkDisplayPreview,
  'links-input': LinkInput,
  'links-icon-input': LinkIconInput,
  'links-list': LinksList,
  'links-item': LinksItem,
  'links-item-icon': LinksItemIcon,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
