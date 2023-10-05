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

export function getNotifications(options) {
  const formData = new FormData();
  if (options?.plugins) {
    options?.plugins.forEach(p => formData.append('plugin', p));
  }
  if (options?.offset && options?.offset > 0) {
    formData.append('offset', options?.offset);
  }
  if (options?.limit && options?.limit > 0) {
    formData.append('limit', options?.limit);
  }
  if (options?.includeHidden) {
    formData.append('includeHidden', 'true');
  }
  if (options?.badgesByPlugin) {
    formData.append('badgeByPlugin', 'true');
  }
  if (options?.unreadOnly) {
    formData.append('onlyUnread', 'true');
  }
  const params = decodeURIComponent(new URLSearchParams(formData).toString());
  return fetch(`/portal/rest/notifications/webNotifications?${params}`, {
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

export function resetBadge(plugins) {
  const formData = new FormData();
  if (plugins) {
    plugins.forEach(p => formData.append('plugin', p));
  }
  const params = plugins?.length && decodeURIComponent(new URLSearchParams(formData).toString());
  return fetch(`/portal/rest/notifications/webNotifications?operation=resetBadge${params && '&' || ''}${params || ''}`, {
    method: 'PATCH',
    credentials: 'include',
  }).then((resp) => {
    if (!resp.ok) {
      throw new Error('Error processing request on server');
    }
  });
}

export function markAllAsRead(plugins) {
  const formData = new FormData();
  if (plugins) {
    plugins.forEach(p => formData.append('plugin', p));
  }
  const params = plugins?.length && decodeURIComponent(new URLSearchParams(formData).toString());
  return fetch(`/portal/rest/notifications/webNotifications?operation=markAllAsRead${params && '&' || ''}${params || ''}`, {
    method: 'PATCH',
    credentials: 'include',
  }).then((resp) => {
    if (!resp.ok) {
      throw new Error('Error processing request on server');
    }
  });
}

export function markRead(id) {
  return fetch(`/portal/rest/notifications/webNotifications/${id || ''}?operation=markAsRead`, {
    credentials: 'include',
    method: 'PATCH',
  }).then((resp) => {
    if (!resp.ok) {
      throw new Error('Error processing request on server');
    }
  });
}

export function hideNotification(id) {
  return fetch(`/portal/rest/notifications/webNotifications/${id || ''}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then((resp) => {
    if (!resp.ok) {
      throw new Error('Error processing request on server');
    }
  });
}
