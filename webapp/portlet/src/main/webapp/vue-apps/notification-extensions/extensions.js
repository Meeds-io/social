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

extensionRegistry.registerExtension('WebNotification', 'notification-group-extension', {
  rank: 10,
  name: 'usersAndSpaces',
  plugins: [
    'NewUserPlugin',
    'RelationshipReceivedRequestPlugin',
    'SpaceInvitationPlugin',
    'RequestJoinSpacePlugin'
  ],
  icon: 'fa-people-arrows',
});
extensionRegistry.registerExtension('WebNotification', 'notification-group-extension', {
  rank: 20,
  name: 'stream',
  plugins: [
    'PostActivityPlugin',
    'PostActivitySpaceStreamPlugin',
    'ActivityCommentPlugin',
    'ActivityCommentWatchPlugin',
    'ActivityReplyToCommentPlugin',
    'ActivityMentionPlugin',
    'SharedActivitySpaceStreamPlugin',
    'EditActivityPlugin',
    'LikePlugin',
    'EditCommentPlugin',
    'LikeCommentPlugin',
  ],
  icon: 'fa-stream',
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'NewUserPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-new-user'],
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'RelationshipReceivedRequestPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-relationship-received-request'],
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'SpaceInvitationPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-space-invitation'],
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'RequestJoinSpacePlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-space-join-request'],
});

extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'PostActivityPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-activity-post'],
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'PostActivitySpaceStreamPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-activity-post-space'],
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'ActivityCommentPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-activity-comment'],
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'ActivityCommentWatchPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-activity-comment'],
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'ActivityReplyToCommentPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-activity-comment-reply'],
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'ActivityMentionPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-activity-mention'],
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'SharedActivitySpaceStreamPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-activity-share'],
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'EditActivityPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-activity-edit'],
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'LikePlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-activity-like'],
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'EditCommentPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-comment-edit'],
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'LikeCommentPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-comment-like'],
});
