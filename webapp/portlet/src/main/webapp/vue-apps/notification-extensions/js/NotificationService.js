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

export function getNotifications() {
  return fetch('/portal/rest/notifications/webNotifications', {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting notification list');
    }
  });
}

export function resetBadge() {
  return fetch('/portal/rest/notifications/webNotifications?operation=resetBadge', {
    headers: {
    },
    method: 'PATCH',
    credentials: 'include',
  });
}

export function markAllAsRead() {
  return fetch('/portal/rest/notifications/webNotifications?operation=markAllAsRead', {
    headers: {
    },
    method: 'PATCH',
    credentials: 'include',
  });
}

export function markRead(id) {
  return fetch(`/portal/rest/notifications/webNotifications/${id || ''}?operation=markAsRead`, {
    headers: {
      credentials: 'include',
    },
    method: 'PATCH',
  });
}

export function hideNotification(id) {
  return fetch(`/portal/rest/notifications/webNotifications/${id || ''}`, {
    headers: {
    },
    method: 'DELETE',
    credentials: 'include',
  });
}
