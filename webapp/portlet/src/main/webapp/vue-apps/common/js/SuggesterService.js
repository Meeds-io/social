import {getIdentityByProviderIdAndRemoteId, getIdentityById} from './IdentityService.js';

export function searchSpacesOrUsers(filter, result, typeOfRelations, searchOptions, includeUsers, includeSpaces, onlyRedactor, noRedactorSpace, onlyManager, searchStartedCallback, searchEndCallback) {
  if (includeSpaces) {
    searchStartedCallback('space');
    searchSpaces(filter, result, onlyRedactor, noRedactorSpace, onlyManager)
      .finally(() => searchEndCallback && searchEndCallback('space'));
  }
  if (includeUsers) {
    searchStartedCallback('organization');
    searchUsers(filter, result, typeOfRelations, searchOptions)
      .finally(() => searchEndCallback && searchEndCallback('organization'));
  }
}

function searchSpaces(filter, items, excludeNonRedactor, noRedactorSpace, excludeNonManager) {
  const formData = new FormData();
  formData.append('filterType', 'member');
  formData.append('limit', '20');
  formData.append('q', filter);
  const params = new URLSearchParams(formData).toString();

  return fetch(`/portal/rest/v1/social/spaces?${params}`, {credentials: 'include'})
    .then(resp => resp && resp.ok && resp.json())
    .then(data => {
      data.spaces.forEach((item) => {
        if ((!noRedactorSpace || !item.redactorsCount) && (!excludeNonRedactor || item.isRedactor || !item.redactorsCount) || (!excludeNonManager || item.canEdit)) {
          items.push({
            id: `space:${item.prettyName}`,
            remoteId: item.prettyName,
            providerId: 'space',
            spaceId: item.id,
            displayName: item.displayName,
            profile: {
              fullName: item.displayName,
              originalName: item.shortName,
              avatarUrl: item.avatarUrl ? item.avatarUrl : `/portal/rest/v1/social/spaces/${item.prettyName}/avatar`,
            },
          });
        }
      });
    });
}

function searchUsers(filter, items, typeOfRelation, searchOptions) {
  const options = {
    nameToSearch: filter,
    typeOfRelation: typeOfRelation || 'mention_activity_stream',
    currentUser: eXo.env.portal.userName,
  };

  let params = null;
  let url = null;
  if (searchOptions && !searchOptions.searchUrl) {
    Object.assign(options, searchOptions);
    params = $.param(options);
    url = '/portal/rest/social/people/suggest.json?'.concat(params);
  } else {
    if (searchOptions.options) {
      params = $.param(searchOptions.options);
      url = searchOptions.searchUrl.concat(`${filter}?${params}`);
    } else {
      url = searchOptions.searchUrl.concat(filter);
    }

  }

  return fetch(url, {credentials: 'include'})
    .then(resp => resp && resp.ok && resp.json())
    .then(data => {
      if (data) {
        data = data.options || data;
        if (data && data.length) {
          data.forEach((item) => {
            let username = item.value || item.id;
            if (item.id && item.id.indexOf('@') === 0){
              username = item.id.substring(1);
            }
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
      originalName: profile.originalName,
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
