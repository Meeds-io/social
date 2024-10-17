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
import SpaceTemplatesManagement from './components/SpaceTemplatesManagement.vue';

import Toolbar from './components/header/Toolbar.vue';
import SpaceTemplates from './components/list/SpaceTemplates.vue';

import SpaceTemplateItem from './components/list/SpaceTemplateItem.vue';
import SpaceTemplateItemMenu from './components/list/SpaceTemplateItemMenu.vue';
import SpaceTemplateItemPermission from './components/list/SpaceTemplateItemPermission.vue';

import SpaceTemplateNameDrawer from './components/drawer/SpaceTemplateNameDrawer.vue';
import SpaceTemplateCharacteristicsDrawer from './components/drawer/SpaceTemplateCharacteristicsDrawer.vue';

import SpaceTemplateBanner from './components/form/SpaceTemplateBanner.vue';
import SpaceTemplateVisibility from './components/form/SpaceTemplateVisibility.vue';
import SpaceTemplateAccess from './components/form/SpaceTemplateAccess.vue';
import SpaceTemplatePermissions from './components/form/SpaceTemplatePermissions.vue';
import SpaceTemplatePermissionsEditorial from './components/form/SpaceTemplatePermissionsEditorial.vue';

const components = {
  'space-templates-management': SpaceTemplatesManagement,
  'space-templates-management-toolbar': Toolbar,
  'space-templates-management-list': SpaceTemplates,
  'space-templates-management-item': SpaceTemplateItem,
  'space-templates-management-item-menu': SpaceTemplateItemMenu,
  'space-templates-management-item-permission': SpaceTemplateItemPermission,
  'space-templates-management-name-drawer': SpaceTemplateNameDrawer,
  'space-templates-management-characteristics-drawer': SpaceTemplateCharacteristicsDrawer,
  'space-templates-management-banner': SpaceTemplateBanner,
  'space-templates-management-visibility': SpaceTemplateVisibility,
  'space-templates-management-access': SpaceTemplateAccess,
  'space-templates-management-permissions': SpaceTemplatePermissions,
  'space-templates-management-permissions-editorial': SpaceTemplatePermissionsEditorial,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
