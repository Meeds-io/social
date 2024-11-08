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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
extensionRegistry.registerExtension('space-templates', 'space-templates-item-action', {
  rank: 10,
  name: 'create-space',
  componentName: 'space-templates-management-menu-item-create-space',
});
extensionRegistry.registerExtension('space-templates', 'space-templates-item-action', {
  rank: 20,
  name: 'list-spaces',
  componentName: 'space-templates-management-menu-item-list-spaces',
});
extensionRegistry.registerExtension('space-templates', 'space-templates-item-action', {
  rank: 30,
  name: 'edit',
  componentName: 'space-templates-management-menu-item-edit',
});
extensionRegistry.registerExtension('space-templates', 'space-templates-item-action', {
  rank: 40,
  name: 'duplicate',
  componentName: 'space-templates-management-menu-item-duplicate',
});
extensionRegistry.registerExtension('space-templates', 'space-templates-item-action', {
  rank: 50,
  name: 'delete',
  componentName: 'space-templates-management-menu-item-delete',
});

extensionRegistry.registerExtension('spaces-administration', 'main', {
  rank: 200,
  id: 'space-templates-management-name',
  name: 'space-templates-management-name',
  componentName: 'space-templates-management-name-drawer',
});
extensionRegistry.registerExtension('spaces-administration', 'main', {
  rank: 210,
  id: 'space-templates-management-characteristics',
  name: 'space-templates-management-characteristics',
  componentName: 'space-templates-management-characteristics-drawer',
});
