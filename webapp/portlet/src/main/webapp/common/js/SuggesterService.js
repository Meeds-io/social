/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
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
import {getIdentityByProviderIdAndRemoteId, getIdentityById} from './IdentityService.js';

export function searchSpacesOrUsers(filter, result, typeOfRelations, searchOptions, includeUsers, includeSpaces, searchStartedCallback, searchEndCallback) {
  if (includeSpaces) {
    searchStartedCallback('space');
    searchSpaces(filter, result)
      .finally(() => searchEndCallback && searchEndCallback('space'));
  }
  if (includeUsers) {
    searchStartedCallback('organization');
    searchUsers(filter, result, typeOfRelations, searchOptions)
      .finally(() => searchEndCallback && searchEndCallback('organization'));
  }
}

function searchSpaces(filter, items) {
  const params = $.param({fields: ['id', 'prettyName', 'displayName', 'avatarUrl'], keyword: filter});
  return fetch(`/portal/rest/space/user/searchSpace?${params}`, {credentials: 'include'})
    .then(resp => resp && resp.ok && resp.json())
    .then(data => {
      data.forEach((item) => {
        items.push({
          id: `space:${item.prettyName}`,
          remoteId: item.prettyName,
          providerId: 'space',
          profile: {
            fullName: item.displayName,
            avatarUrl: item.avatarUrl ? item.avatarUrl : `/portal/rest/v1/social/spaces/${item.prettyName}/avatar`,
          },
        });
      });
    });
}

function searchUsers(filter, items, typeOfRelation, searchOptions) {
  const options = {
    nameToSearch: filter,
    typeOfRelation: typeOfRelation || 'mention_activity_stream',
    currentUser: eXo.env.portal.userName,
  };
  if (searchOptions) {
    Object.assign(options, searchOptions);
  }
  const params = $.param(options);
  return fetch(`/portal/rest/social/people/suggest.json?${params}`, {credentials: 'include'})
    .then(resp => resp && resp.ok && resp.json())
    .then(data => {
      if (data) {
        data = data.options || data;
        if (data && data.length) {
          data.forEach((item) => {
            const username = item.value || item.id  && item.id.indexOf('@') === 0 && item.id.substring(1);
            items.push({
              id: `organization:${username}`,
              remoteId: username,
              providerId: 'organization',
              profile: {
                fullName: item.name || item.text,
                avatarUrl: item.avatar || item.avatarUrl || `/portal/rest/v1/social/users/${username}/avatar`,
              },
            });
          });
        }
      }
    });
}

export function convertIdentityToSuggesterItem(identity) {
  if (!identity) {
    return null;
  }

  const suggesterIdentity = {
    id: `${identity.providerId}:${identity.remoteId}`,
    providerId: identity.providerId,
    remoteId: identity.remoteId
  };

  const profile = identity.profile || identity.space;
  if (profile) {
    suggesterIdentity.profile = {
      avatarUrl: profile.avatarUrl || profile.avatar,
      fullName: profile.displayName || profile.fullname || profile.fullName,
    };
  }
  return suggesterIdentity;
}

export function convertSuggesterItemToIdentity(suggesterIdentity) {
  if (!convertSuggesterItemToIdentity) {
    return null;
  }
  
  const identity = {
    providerId: suggesterIdentity.providerId,
    remoteId: suggesterIdentity.remoteId,
  };
  
  const profile = suggesterIdentity.profile;
  if (profile) {
    identity.profile = {
      avatar: profile.avatarUrl,
      fullname: profile.fullName,
    };
  }
  return identity;
}

export function getSuggesterItemByIdentityId(identityId) {
  if (!identityId) {
    return Promise.resolve(null);
  }

  return getIdentityById(identityId)
    .then(convertIdentityToSuggesterItem);
}

export function getSuggesterItemToIdentity(suggesterIdentity) {
  if (!suggesterIdentity || !suggesterIdentity.providerId || !suggesterIdentity.remoteId) {
    return Promise.resolve(null);
  }

  return getIdentityByProviderIdAndRemoteId(suggesterIdentity.providerId, suggesterIdentity.remoteId);
}
