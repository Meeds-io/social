/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
import SubspacesList from './components/SubspacesList.vue';
import SubspacesListDrawer from './components/SubspacesListDrawer.vue';
import SubspacesListItem from './components/SubspacesListItem.vue';
import SubspacesListSettingsDrawer from './components/SubspacesListSettingsDrawer.vue';

import * as subspacesListService from './subspacesListService.js';

const components = {
  'subspaces-list': SubspacesList,
  'subspaces-list-drawer': SubspacesListDrawer,
  'subspaces-list-item': SubspacesListItem,
  'subspaces-list-settings-drawer': SubspacesListSettingsDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

if (!Vue.prototype.$subspacesListService) {
  window.Object.defineProperty(Vue.prototype, '$subspacesListService', {
    value: subspacesListService,
  });
}
