/*
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association contact@meeds.io
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import TopBarNavigationMenu from './components/TopBarNavigationMenu.vue';
import NavigationMenuItem from './components/NavigationMenuItem.vue';
import NavigationMenuSubItem from './components/NavigationMenuSubItem.vue';
import NavigationMobileMenuItem from './components/mobile/NavigationMobileMenuItem.vue';
import NavigationMobileMenuSubItem from './components/mobile/NavigationMobileMenuSubItem.vue';

const components = {
  'top-bar-navigation-menu': TopBarNavigationMenu,
  'navigation-menu-item': NavigationMenuItem,
  'navigation-menu-sub-item': NavigationMenuSubItem,
  'navigation-mobile-menu-item': NavigationMobileMenuItem,
  'navigation-mobile-menu-sub-item': NavigationMobileMenuSubItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
