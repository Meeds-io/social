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

export function getSettings(name, lang) {
  const formData = new FormData();
  if (lang) {
    formData.append('lang', lang);
  }
  const urlParams = new URLSearchParams(formData).toString();
  const params = urlParams.length && `?${decodeURIComponent(urlParams)}` || '';
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/social/links/${name}${params}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else if (resp?.status === 404) {
      return null;
    } else {
      throw new Error('Error getting links setting');
    }
  });
}

export function saveSettings(settings) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/social/links/${settings.name}`, {
    headers: {
      'Content-Type': 'application/json',
    },
    method: 'PUT',
    credentials: 'include',
    body: JSON.stringify(settings),
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Error saving links setting');
    }
  });
}

export function saveSettingName(url, name) {
  return fetch(url, {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    method: 'POST',
    credentials: 'include',
    body: `name=${name}`,
  }).then((resp) => {
    if (!resp?.ok) {
      throw new Error('Error saving settings');
    }
  });
}

export function toLinkUrl(url) {
  if (url?.indexOf?.('/') === 0) {
    url = `${window.location.origin}${url}`;
  }
  const useNonSSL = url?.indexOf('http://') === 0;
  url = Autolinker.parse(url || '', {
    urls: true,
    email: false,
    phone: false,
    mention: false,
    hashtag: false,
  })?.[0]?.getUrl?.()?.replace?.('javascript:', '');
  if (useNonSSL) {
    return url;
  } else {
    return url?.replace?.('http://', 'https://');
  }
}