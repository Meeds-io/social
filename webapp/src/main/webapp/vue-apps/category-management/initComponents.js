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
import CategoryManagement from './components/CategoryManagement.vue';

import Toolbar from './components/header/Toolbar.vue';

import Tree from './components/list/Tree.vue';
import ItemMenu from './components/list/ItemMenu.vue';

import ItemMenuAdd from './components/menu-action/ItemMenuAdd.vue';
import ItemMenuEdit from './components/menu-action/ItemMenuEdit.vue';
import ItemMenuMove from './components/menu-action/ItemMenuMove.vue';
import ItemMenuDelete from './components/menu-action/ItemMenuDelete.vue';

import CategoryFormDrawer from './components/drawer/CategoryFormDrawer.vue';

import Permissions from './components/form/Permissions.vue';

const components = {
  'category-management': CategoryManagement,

  'category-management-toolbar': Toolbar,

  'category-management-tree': Tree,
  'category-management-item-menu': ItemMenu,
  'category-management-menu-item-add': ItemMenuAdd,
  'category-management-menu-item-edit': ItemMenuEdit,
  'category-management-menu-item-move': ItemMenuMove,
  'category-management-menu-item-delete': ItemMenuDelete,

  'category-management-form-drawer': CategoryFormDrawer,

  'category-management-permissions': Permissions,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

//get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpacesList');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}
