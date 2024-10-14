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
extensionRegistry.registerExtension('profile-extension', 'action', {
  id: 'user-card-open-profile',
  titleKey: 'peopleList.button.openProfile',
  icon: 'fa-external-link-alt',
  order: 1,
  enabled: (user, spaceId) => spaceId && user.enabled,
  link: (user) => {
    return `${window.location.origin}${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${user.username}`;
  },
});

extensionRegistry.registerExtension('profile-extension', 'action', {
  id: 'user-card-copy-profile-link',
  titleKey: 'peopleList.button.copyLink',
  icon: 'fa-link',
  order: 2,
  enabled: (user, spaceId) => spaceId && user.enabled,
  click: (user) => {
    navigator.clipboard.writeText(`${window.location.origin}${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${user.username}`);
    document.dispatchEvent(new CustomEvent('alert-message', {detail: {
      alertType: 'success',
      alertMessageKey: 'peopleList.button.linkCopied',
    }}));
  },
});
