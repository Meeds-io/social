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
import CategoryInput from './components/CategoryInput.vue';
import CategorySuggester from './components/CategorySuggester.vue';

import CategoriesDrawer from './components/drawer/CategoriesDrawer.vue';
import BulkCategoriesDrawer from './components/drawer/BulkCategoriesDrawer.vue';
import CategoryFormDrawer from './components/drawer/CategoryFormDrawer.vue';
import CategoryListDrawer from './components/drawer/CategoryListDrawer.vue';
import CategoryInputDrawer from './components/drawer/CategoryInputDrawer.vue';
import EntryListDrawer from './components/drawer/EntryListDrawer.vue';
import EntryListItem from './components/drawer/EntryListItem.vue';

import CategoriesFilter from './components/CategoriesFilter.vue';
import CategoriesBreadcrumb from './components/filter/CategoriesBreadcrumb.vue';
import CategoryChipsGroup from './components/filter/CategoryChipsGroup.vue';
import CategoryChip from './components/filter/CategoryChip.vue';
import CategoryTabsGroup from './components/filter/CategoryTabsGroup.vue';
import CategoryTab from './components/filter/CategoryTab.vue';

const components = {
  'category-input': CategoryInput,

  'category-input-drawer': CategoryInputDrawer,
  'categories-drawer': CategoriesDrawer,
  'categories-bulk-edit-drawer': BulkCategoriesDrawer,
  'category-form-drawer': CategoryFormDrawer,
  'categories-list-drawer': CategoryListDrawer,
  'category-entry-drawer': EntryListDrawer,
  'category-entry-list-item': EntryListItem,

  'category-suggester': CategorySuggester,
  'categories-filter': CategoriesFilter,
  'categories-breadcrumb': CategoriesBreadcrumb,
  'category-chips-group': CategoryChipsGroup,
  'category-chip': CategoryChip,
  'category-tabs-group': CategoryTabsGroup,
  'category-tab': CategoryTab,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
