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
import UserNotificationMenu from './components/UserNotificationMenu.vue';
import UserNotificationTemplate from './components/UserNotificationTemplate.vue';
import UserNotifications from './components/UserNotifications.vue';
import UserNotificationsList from './components/UserNotificationsList.vue';
import UserNotificationTypes from './components/UserNotificationTypes.vue';
import UserNotificationType from './components/UserNotificationType.vue';
import UserEmptyNotifications from './components/UserEmptyNotifications.vue';

import NewUser from './components/plugins/NewUser.vue';
import RelationshipReceivedRequestPlugin from './components/plugins/RelationshipReceivedRequestPlugin.vue';
import SpaceInvitationPlugin from './components/plugins/SpaceInvitationPlugin.vue';
import RequestJoinSpacePlugin from './components/plugins/RequestJoinSpacePlugin.vue';

import ActivityCommentPlugin from './components/plugins/ActivityCommentPlugin.vue';
import ActivityMentionPlugin from './components/plugins/ActivityMentionPlugin.vue';
import ActivityReplyToCommentPlugin from './components/plugins/ActivityReplyToCommentPlugin.vue';
import EditActivityPlugin from './components/plugins/EditActivityPlugin.vue';
import EditCommentPlugin from './components/plugins/EditCommentPlugin.vue';
import LikeCommentPlugin from './components/plugins/LikeCommentPlugin.vue';
import LikePlugin from './components/plugins/LikePlugin.vue';
import PostActivityPlugin from './components/plugins/PostActivityPlugin.vue';
import PostActivitySpaceStreamPlugin from './components/plugins/PostActivitySpaceStreamPlugin.vue';
import SharedActivitySpaceStreamPlugin from './components/plugins/SharedActivitySpaceStreamPlugin.vue';

import ActivityBasedPlugin from './components/plugins/abstract/ActivityBasedPlugin.vue';

const components = {
  'user-notification-html': HtmlNotification,
  'user-notifications': UserNotifications,
  'user-notifications-list': UserNotificationsList,
  'user-notification-types': UserNotificationTypes,
  'user-notification-type': UserNotificationType,
  'user-notification-empty': UserEmptyNotifications,
  'user-notification': UserNotification,
  'user-notification-template': UserNotificationTemplate,
  'user-notification-menu': UserNotificationMenu,
  'user-notification-new-user': NewUser,
  'user-notification-relationship-received-request': RelationshipReceivedRequestPlugin,
  'user-notification-space-invitation': SpaceInvitationPlugin,
  'user-notification-space-join-request': RequestJoinSpacePlugin,

  'user-notification-activity-post': PostActivityPlugin,
  'user-notification-activity-post-space': PostActivitySpaceStreamPlugin,
  'user-notification-activity-comment': ActivityCommentPlugin,
  'user-notification-activity-comment-reply': ActivityReplyToCommentPlugin,
  'user-notification-activity-mention': ActivityMentionPlugin,
  'user-notification-activity-share': SharedActivitySpaceStreamPlugin,
  'user-notification-activity-edit': EditActivityPlugin,
  'user-notification-activity-like': LikePlugin,
  'user-notification-comment-edit': EditCommentPlugin,
  'user-notification-comment-like': LikeCommentPlugin,

  'user-notification-activity-base': ActivityBasedPlugin,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
