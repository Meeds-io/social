/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
let translationConfiguration = null;

export function getTranslationConfiguration() {
  if (translationConfiguration) {
    return Promise.resolve(JSON.parse(JSON.stringify(translationConfiguration)));
  } else {
    return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/social/translations/configuration`, {
      method: 'GET',
      credentials: 'include',
    }).then((resp) => {
      if (resp?.ok) {
        return resp.json();
      } else {
        throw new Error('Error when getting translation configuration');
      }
    }).then((configuration) => {
      translationConfiguration = configuration;
      return JSON.parse(JSON.stringify(translationConfiguration));
    });
  }
}

export function getTranslations(objectType, objectId, fieldName) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/social/translations/${objectType}/${objectId}/${fieldName}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error(`Error when getting list of translations for field ${objectType}/${objectId}/${fieldName}`);
    }
  });
}

export function saveTranslations(objectType, objectId, fieldName, labels) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/social/translations/${objectType}/${objectId}/${fieldName}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(labels),
  }).then((resp) => {
    if (!resp?.ok) {
      throw new Error(`Error when saving the list of translations for field ${objectType}/${objectId}/${fieldName}`);
    }
  });
}

export function saveDefaultLanguage(lang) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/social/translations/configuration/defaultLanguage`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: `lang=${lang}`,
  }).then((resp) => {
    if (!resp?.ok) {
      throw new Error(`Error when saving default language '${lang}' configuration`);
    }
  });
}