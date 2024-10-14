/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
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

import ComplementaryFilter from './components/ComplementaryFilter.vue';
import ComplementaryFilterItem from './components/ComplementaryFilterItem.vue';
import ComplementaryFilterItemsDrawer from './components/ComplementaryFilterItemsDrawer.vue';

import * as complementaryFilterService from './js/ComplementaryFilterService.js';


const components = {
  'complementary-filter': ComplementaryFilter,
  'complementary-filter-item': ComplementaryFilterItem,
  'complementary-filter-items-drawer': ComplementaryFilterItemsDrawer
};

for (const key in components) {
  Vue.component(key, components[key]);
}

if (!Vue.prototype.$complementaryFilterService) {
  window.Object.defineProperty(Vue.prototype, '$complementaryFilterService', {
    value: complementaryFilterService,
  });
}
