import {getIdentityByProviderIdAndRemoteId, getIdentityById} from './IdentityService.js';

export function searchSpacesOrUsers(filter, result, typeOfRelations, searchOptions, includeUsers, includeSpaces, onlyRedactor, excludeRedactionalSpace, onlyManager, searchStartedCallback, searchEndCallback) {
  if (includeSpaces) {
    searchStartedCallback('space');
    searchSpaces(filter, result, onlyRedactor, excludeRedactionalSpace, onlyManager , searchOptions?.filterType )
      .finally(() => searchEndCallback && searchEndCallback('space'));
  }
  if (includeUsers) {
    searchStartedCallback('organization');
    searchUsers(filter, result, typeOfRelations, searchOptions)
      .finally(() => searchEndCallback && searchEndCallback('organization'));
  }
}

export function search(filter) {
  if (filter.includeSpaces || filter.includeUsers) {
    searchSpacesOrUsers(filter.term, filter.items, filter.typeOfRelations, filter.searchOptions, filter.includeUsers, filter.includeSpaces, filter.onlyRedactor, filter.noRedactorSpace, filter.onlyManager, filter.loadingCallback, filter.successCallback);
  }
  if (filter.includeGroups) {
    filter.loadingCallback('group');
    searchGroups(filter.term, filter.groupMember, filter.groupType, filter.items, filter.allGroupsForAdmin, filter.errorCallback)
      .finally(() => filter.successCallback && filter.successCallback('group'));
  }
}
/*
* onlyRedactor : search spaces where the user is a redactor
* excludeRedactionalSpace : space spaces that have no member promoted as redactor
* onlyManager : search spaces where the user is a manager
*/
function searchSpaces(filter, items, onlyRedactor, excludeRedactionalSpace, onlyManager,filterType) {
  const formData = new FormData();
  formData.append('filterType', filterType || 'member');
  formData.append('limit', '20');
  formData.append('q', filter);
  const params = new URLSearchParams(formData).toString();

  return fetch(`/portal/rest/v1/social/spaces?${params}`, {credentials: 'include'})
    .then(resp => resp && resp.ok && resp.json())
    .then(data => {
      data.spaces.forEach((item) => {
        if ((excludeRedactionalSpace && !item.redactorsCount ) || ((!onlyRedactor || item.isRedactor || item.canEdit || !item.redactorsCount) && (!onlyManager || item.canEdit))) {
          items.push({
            id: `space:${item.prettyName}`,
            remoteId: item.prettyName,
            spaceId: item.id,
            groupId: item.groupId,
            providerId: 'space',
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

function searchGroups(filter, groupMember, groupType, items, allGroupsForAdmin, errorCallback) {
  const formData = new FormData();
  formData.append('q', filter);
  formData.append('groupMember', groupMember);
  formData.append('groupType', groupType);
  formData.append('allGroupsForAdmin', allGroupsForAdmin);
  formData.append('excludeParentGroup', '/spaces');
  formData.append('excludeParentGroup', '/');
  const params = new URLSearchParams(formData).toString();

  return fetch(`/portal/rest/v1/groups/treeMembers?${params}`, { credentials: 'include' })
    .then(resp => resp && resp.ok && resp.json())
    .catch((e) => {
      errorCallback(e);
    })
    .then(data => {
      data.entities.forEach((item) => {
        items.push({
          id: `group:${item.groupName}`,
          remoteId: item.groupName,
          spaceId: item.id,
          providerId: 'group',
          displayName: item.label,
          profile: {
            fullName: item.label,
            originalName: item.groupName,
            avatarUrl: null,
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
              identityId: item.identityId,
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
