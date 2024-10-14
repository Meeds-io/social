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
import PeopleList from './components/PeopleList.vue';
import PeopleCardList from './components/PeopleCardList.vue';
import PeopleToolbar from './components/PeopleToolbar.vue';
import PeopleAdvancedFilterDrawer from './components/PeopleAdvancedFilterDrawer.vue';
import AdvancedFilterInputItem from './components/AdvancedFilterInputItem.vue';
import PeopleCard from './components/usercard/PeopleCard.vue';
import PeopleUserCompactCard from './components/usercard/PeopleUserCompactCard.vue';
import PeopleUserCard from './components/usercard/PeopleUserCard.vue';
import PeopleUserRole from './components/usercard/PeopleUserRole.vue';
import PeopleUserMenuItem from './components/usercard/PeopleUserMenuItem.vue';
import PeopleUserCardRoles from './components/usercard/PeopleUserCardRoles.vue';
import PeopleUserMenu from './components/usercard/PeopleUserMenu.vue';

const components = {
  'people-list': PeopleList,
  'people-card-list': PeopleCardList,
  'people-toolbar': PeopleToolbar,
  'people-card': PeopleCard,
  'people-advanced-filter-drawer': PeopleAdvancedFilterDrawer,
  'people-advanced-filter-input-item': AdvancedFilterInputItem,
  'people-user-compact-card': PeopleUserCompactCard,
  'people-user-card': PeopleUserCard,
  'people-user-role': PeopleUserRole,
  'people-user-card-roles': PeopleUserCardRoles,
  'people-user-menu': PeopleUserMenu,
  'people-user-menu-item': PeopleUserMenuItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
//get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('peopleList');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}
