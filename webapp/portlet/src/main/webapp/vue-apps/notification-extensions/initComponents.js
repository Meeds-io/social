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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

import HtmlNotification from './components/HtmlNotification.vue';
import UserNotification from './components/UserNotification.vue';
import UserNotificationTemplate from './components/UserNotificationTemplate.vue';
import UserNotifications from './components/UserNotifications.vue';
import UserNotificationsList from './components/UserNotificationsList.vue';
import UserEmptyNotifications from './components/UserEmptyNotifications.vue';

import NewUser from './components/plugins/NewUser.vue';

const components = {
  'user-notification-html': HtmlNotification,
  'user-notifications': UserNotifications,
  'user-notifications-list': UserNotificationsList,
  'user-notification-empty': UserEmptyNotifications,
  'user-notification': UserNotification,
  'user-notification-template': UserNotificationTemplate,
  'user-notification-new-user': NewUser,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
