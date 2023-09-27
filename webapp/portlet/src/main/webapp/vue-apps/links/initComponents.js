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
import LinkFormDrawer from './components/settings/LinkFormDrawer.vue';
import LinkDisplayPreview from './components/settings/LinkDisplayPreview.vue';
import LinkInput from './components/settings/LinkInput.vue';
import LinkIconInput from './components/settings/LinkIconInput.vue';

import LinksList from './components/view/LinksList.vue';
import LinksHeader from './components/view/LinksHeader.vue';
import LinksIcon from './components/view/LinksIcon.vue';
import LinksColumn from './components/view/LinksColumn.vue';
import LinksCard from './components/view/LinksCard.vue';

const components = {
  'links-app': LinksApp,
  'links-settings-drawer': LinkSettingsDrawer,
  'links-form-drawer': LinkFormDrawer,
  'links-display-preview': LinkDisplayPreview,
  'links-input': LinkInput,
  'links-icon-input': LinkIconInput,

  'links-list': LinksList,
  'links-header': LinksHeader,
  'links-icon': LinksIcon,
  'links-column': LinksColumn,
  'links-card': LinksCard,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
