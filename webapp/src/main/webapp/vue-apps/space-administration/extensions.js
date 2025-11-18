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
extensionRegistry.registerExtension('spaces-administration', 'menu-action', {
  rank: 10,
  name: 'open-settings',
  componentName: 'spaces-administration-settings-menu-item',
});

extensionRegistry.registerExtension('spaces-administration', 'menu-action', {
  rank: 20,
  name: 'sync-members',
  componentName: 'spaces-administration-sync-members-menu-item',
});

extensionRegistry.registerExtension('spaces-administration', 'menu-action', {
  rank: 30,
  name: 'manage-relationships',
  componentName: 'spaces-administration-relationship-menu-item',
});

extensionRegistry.registerExtension('spaces-administration', 'menu-action', {
  rank: 40,
  name: 'apply-template',
  componentName: 'spaces-administration-edit-categories-menu-item',
});

extensionRegistry.registerExtension('spaces-administration', 'menu-action', {
  rank: 50,
  name: 'apply-template',
  componentName: 'spaces-administration-apply-template-menu-item',
});

extensionRegistry.registerExtension('spaces-administration', 'menu-action', {
  rank: 60,
  name: 'save-as-template',
  componentName: 'spaces-administration-save-as-template-menu-item',
});

extensionRegistry.registerExtension('spaces-administration', 'menu-action', {
  rank: 70,
  name: 'permissions',
  componentName: 'spaces-administration-permissions-menu-item',
});

extensionRegistry.registerExtension('spaces-administration', 'menu-action', {
  rank: 80,
  name: 'delete',
  componentName: 'spaces-administration-delete-menu-item',
});

extensionRegistry.registerExtension('spaces-administration', 'bulk-action', {
  rank: 10,
  name: 'sync-members',
  componentName: 'spaces-administration-bulk-sync-members',
});

extensionRegistry.registerExtension('spaces-administration', 'bulk-action', {
  rank: 20,
  name: 'edit-categories',
  componentName: 'spaces-administration-bulk-edit-categories',
});

extensionRegistry.registerExtension('spaces-administration', 'bulk-action', {
  rank: 30,
  name: 'apply-template',
  componentName: 'spaces-administration-bulk-apply-template',
});

extensionRegistry.registerExtension('spaces-administration', 'bulk-action', {
  rank: 40,
  name: 'permissions',
  componentName: 'spaces-administration-bulk-permissions',
});

extensionRegistry.registerExtension('spaces-administration', 'bulk-action', {
  rank: 50,
  name: 'delete',
  componentName: 'spaces-administration-bulk-delete',
});
