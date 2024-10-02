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
document.addEventListener('space-member-management-actions-load', () => {
  extensionRegistry.registerExtension('space-member-extension', 'action', {
    id: 'spaceMembers-removeMember',
    titleKey: 'peopleList.button.removeMember',
    iconColor: 'error--text',
    textColor: 'error--text',
    icon: 'fa-user-minus',
    class: 'fas fa-user-minus',
    order: 2,
    enabled: user => {
      return (user.isMember || !user.enabled)
        && user?.username !== eXo?.env?.portal?.userName
        && !user.isGroupBound;
    },
    click: (user, spaceId) => {
      Vue.prototype.$spaceService.removeMember(spaceId, user.username)
        .then(() => {
          document.dispatchEvent(new CustomEvent('people-list-refresh'));
          document.dispatchEvent(new CustomEvent('alert-message', {detail: {
            alertType: 'success',
            alertMessageKey: 'peopleList.alert.memberRemoved',
          }}));
        });
    },
  });
  extensionRegistry.registerExtension('space-member-extension', 'action', {
    id: 'spaceMembers-removeManager',
    titleKey: 'peopleList.button.removeManager',
    icon: 'uiIconMemberAdmin',
    class: 'fas fa-user-cog',
    order: 1,
    enabled: user => user.isManager,
    click: (user, spaceId) => {
      Vue.prototype.$spaceService.removeManager(spaceId, user.username)
        .then(() => {
          document.dispatchEvent(new CustomEvent('people-list-refresh'));
          document.dispatchEvent(new CustomEvent('alert-message', {detail: {
            alertType: 'success',
            alertMessageKey: 'peopleList.alert.managerRemoved',
          }}));
        });
    },
  });
  extensionRegistry.registerExtension('space-member-extension', 'action', {
    id: 'spaceMembers-promoteManager',
    titleKey: 'peopleList.button.promoteManager',
    icon: 'uiIconMemberAdmin',
    class: 'fas fa-user-cog',
    order: 1,
    enabled: user => user.enabled && !user.deleted && !user.isManager,
    click: (user, spaceId) => {
      Vue.prototype.$spaceService.promoteManager(spaceId, user.username)
        .then(() => {
          document.dispatchEvent(new CustomEvent('people-list-refresh'));
          document.dispatchEvent(new CustomEvent('alert-message', {detail: {
            alertType: 'success',
            alertMessageKey: 'peopleList.alert.managerPromoted',
          }}));
        });
    },
  });
  extensionRegistry.registerExtension('space-member-extension', 'action', {
    id: 'spaceMembers-cancelInvitation',
    titleKey: 'peopleList.button.cancelInvitation',
    icon: 'uiIconTrash',
    order: 1,
    enabled: user => user.isInvited,
    click: (user, spaceId) => {
      Vue.prototype.$spaceService.cancelInvitation(spaceId, user.username)
        .then(() => {
          document.dispatchEvent(new CustomEvent('people-list-refresh'));
          document.dispatchEvent(new CustomEvent('alert-message', {detail: {
            alertType: 'success',
            alertMessageKey: 'peopleList.alert.invitationCanceled',
          }}));
        });
    },
  });
  extensionRegistry.registerExtension('space-member-extension', 'action', {
    id: 'spaceMembers-acceptPending',
    titleKey: 'peopleList.button.acceptPending',
    icon: 'uiIconUserCheck',
    order: 1,
    enabled: user => user.isPending,
    click: (user, spaceId) => {
      Vue.prototype.$spaceService.acceptUserRequest(spaceId, user.username)
        .then(() => {
          document.dispatchEvent(new CustomEvent('people-list-refresh'));
          document.dispatchEvent(new CustomEvent('alert-message', {detail: {
            alertType: 'success',
            alertMessageKey: 'peopleList.alert.requestAccepted',
          }}));
        });
    },
  });
  extensionRegistry.registerExtension('space-member-extension', 'action', {
    id: 'spaceMembers-refusePending',
    titleKey: 'peopleList.button.refusePending',
    icon: 'uiIconTrash',
    order: 1,
    enabled: user => user.isPending,
    click: (user, spaceId) => {
      Vue.prototype.$spaceService.refuseUserRequest(spaceId, user.username)
        .then(() => {
          document.dispatchEvent(new CustomEvent('people-list-refresh'));
          document.dispatchEvent(new CustomEvent('alert-message', {detail: {
            alertType: 'success',
            alertMessageKey: 'peopleList.alert.requestRefused',
          }}));
        });
    },
  });
});