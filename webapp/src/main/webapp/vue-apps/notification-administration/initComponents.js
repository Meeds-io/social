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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import NotificationAdministration from './components/NotificationAdministration.vue';

import NotificationPlugins from './components/NotificationPlugins.vue';
import NotificationChannels from './components/NotificationChannels.vue';
import NotificationContact from './components/NotificationContact.vue';

import NotificationPluginGroup from './components/plugin/NotificationPluginGroup.vue';
import NotificationPlugin from './components/plugin/NotificationPlugin.vue';
import NotificationPluginDrawer from './components/plugin/NotificationPluginDrawer.vue';

import NotificationContactDrawer from './components/contact/NotificationContactDrawer.vue';

const components = {
  'notification-administration': NotificationAdministration,
  'notification-administration-plugins': NotificationPlugins,
  'notification-administration-channels': NotificationChannels,
  'notification-administration-contact': NotificationContact,
  'notification-administration-plugin': NotificationPlugin,
  'notification-administration-plugin-group': NotificationPluginGroup,
  'notification-administration-plugin-drawer': NotificationPluginDrawer,
  'notification-administration-contact-drawer': NotificationContactDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
