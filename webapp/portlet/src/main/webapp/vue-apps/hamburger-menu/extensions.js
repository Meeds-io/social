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
extensionRegistry.registerExtension('space-hamburger', 'menu-item', {
  rank: 1,
  name: 'makeAsHomePage',
  icon: 'fa-house-user',
  titleKey: 'menu.spaces.makeAsHomePage',
  enabled: space => eXo.env.portal.homeLink !== `${eXo.env.portal.context}/s/${space.id}`,
  eventName: 'change-home-link-space',
  conserveHover: true,
});
extensionRegistry.registerExtension('space-hamburger', 'menu-item', {
  rank: 2,
  name: 'muteSpace',
  icon: 'far fa-bell-slash',
  titleKey: 'Notification.tooltip.muteSpaceNotification',
  enabled: space => space.isMuted === 'false',
  click: space => {
    return Vue.prototype.$spaceService.muteSpace(space?.id, true)
      .then(() => {
        document.dispatchEvent(new CustomEvent('refresh-notifications'));
        document.dispatchEvent(new CustomEvent('alert-message', {detail: {
          alertMessageKey: 'Notification.alert.successfullyMuted',
          type: 'success',
        }}));
        document.dispatchEvent(new CustomEvent('space-muted', {detail: {
          name: 'spaceLeftNavigationAction',
          spaceId: space.id,
        }}));
        Vue.prototype.$set(space, 'isMuted', 'true');
      })
      .catch(() => {
        document.dispatchEvent(new CustomEvent('alert-message', {detail: {
          alertMessageKey: 'Notification.alert.errorChangingSpaceMutingStatus',
          type: 'error',
        }}));
      });
  },
});
extensionRegistry.registerExtension('space-hamburger', 'menu-item', {
  rank: 2,
  name: 'unMuteSpace',
  icon: 'fa-bell-slash',
  titleKey: 'Notification.tooltip.unmuteSpaceNotification',
  enabled: space => space.isMuted === 'true',
  click: space => {
    return Vue.prototype.$spaceService.muteSpace(space?.id, false)
      .then(() => {
        document.dispatchEvent(new CustomEvent('refresh-notifications'));
        document.dispatchEvent(new CustomEvent('alert-message', {detail: {
          alertMessageKey: 'Notification.alert.successfullyUnmuted',
          type: 'success'
        }}));
        document.dispatchEvent(new CustomEvent('space-unmuted', {detail: {
          name: 'spaceLeftNavigationAction',
          spaceId: space.id,
        }}));
        Vue.prototype.$set(space, 'isMuted', 'false');
      })
      .catch(() => {
        document.dispatchEvent(new CustomEvent('alert-message', {detail: {
          alertMessageKey: 'Notification.alert.errorChangingSpaceMutingStatus',
          type: 'error',
        }}));
      });
  },
});
