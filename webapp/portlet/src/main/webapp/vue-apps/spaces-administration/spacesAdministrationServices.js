export function getSpaces() {
  return fetch(`${Vue.prototype.$spacesConstants.SOCIAL_SPACE_API}?sort=date&order=desc&limit=${Vue.prototype.$spacesConstants.SPACES_PER_PAGE}&returnSize=true&expand=membersCount`, {credentials: 'include'}).then(resp => resp.json());
}

export function searchSpaces(search) {
  return fetch(`${Vue.prototype.$spacesConstants.SOCIAL_SPACE_API}?q=${search}&sort=date&order=desc&limit=${Vue.prototype.$spacesConstants.SPACES_PER_PAGE}&returnSize=true&expand=membersCount`, {credentials: 'include'}).then(resp => resp.json());
}

export function getSpacesPerPage(offset) {
  return fetch(`${Vue.prototype.$spacesConstants.SOCIAL_SPACE_API}?offset=${offset}&sort=date&order=desc&limit=${Vue.prototype.$spacesConstants.SPACES_PER_PAGE}&returnSize=true&expand=membersCount`, {credentials: 'include'}).then(resp => resp.json());
}

export function deleteSpaceById(id) {
  return fetch(`/rest/v1/social/spaces/${id}`, {
    credentials: 'include', 
    method: 'delete'});
}

export function getSpaceLinkSetting(spaceDisplayName, groupId) {
  if (spaceDisplayName && groupId) {
    const spaceName = spaceDisplayName.toLowerCase().split(' ').join('_');
    const groupIdTab = groupId.toLowerCase().split('/');
    const groupName  = groupIdTab[groupIdTab.length-1];
    return `${Vue.prototype.$spacesConstants.PORTAL}${Vue.prototype.$spacesConstants.PROFILE_SPACE_LINK}${groupName}/${spaceName}/settings`;
  } else {
    return null;
  }
}

export function getUserPermissions(userName) {
  return fetch(`${Vue.prototype.$spacesConstants.USER_API}/${userName}`, {credentials: 'include'}).then(resp => resp.json());
}

export function getGroups(query) {
  return fetch(`${Vue.prototype.$spacesConstants.GROUP_API}?q=${query}`, {credentials: 'include'}).then(resp => resp.json());
}

export function getSpacesAdministrationSetting(key) {
  return fetch(`${Vue.prototype.$spacesConstants.SPACES_ADMINISTRATION_API}/permissions/${key}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'GET'
  }).then(resp => {
    const HTTP_OK_CODE = 200;
    if (resp.status === HTTP_OK_CODE) {
      return resp.json();
    } else {
      return resp.text();
    }
  });
}

export function checkCanCreateSpaces() {
  return fetch(`${Vue.prototype.$spacesConstants.SPACES_ADMINISTRATION_API}/permissions/canCreatespaces/${eXo.env.portal.userName}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'GET'
  }).then(resp => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error ('Error to check can add spaces');
    }
  });
}

export function updateSpacesAdministrationSetting(key, value) {
  return fetch(`${Vue.prototype.$spacesConstants.SPACES_ADMINISTRATION_API}/permissions/${key}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: JSON.stringify(value)
  });
}

export function saveGroupsSpaceBindings(spaceId, groupNames) {
  return fetch(`${Vue.prototype.$spacesConstants.SPACE_GROUP_BINDING_API}/saveGroupsSpaceBindings/${spaceId}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify(groupNames)
  });
}

export function getGroupSpaceBindings(spaceId) {
  return fetch(`${Vue.prototype.$spacesConstants.SPACE_GROUP_BINDING_API}/${spaceId}`, {credentials: 'include'}).then(resp => resp.json());
}

export function removeBinding(bindingId) {
  return fetch(`${Vue.prototype.$spacesConstants.SPACE_GROUP_BINDING_API}/removeGroupSpaceBinding/${bindingId}`, {
    credentials: 'include',
    method: 'delete'});
}

export function getGroupsTree() {
  return fetch(`${Vue.prototype.$spacesConstants.SPACE_GROUP_BINDING_API}/getGroupsTree`, {credentials: 'include'}).then(resp => resp.json());
}

export function getBindingReportOperations() {
  return fetch(`${Vue.prototype.$spacesConstants.SPACE_GROUP_BINDING_API}/getBindingReportOperations`, {credentials: 'include'}).then(resp => resp.json());
}

export function getReport(spaceId, action, groupId, groupBindingId) {
  window.open(`${Vue.prototype.$spacesConstants.SPACE_GROUP_BINDING_API}/getExport?spaceId=${spaceId}&action=${action}&group=${groupId}&groupBindingId=${groupBindingId}`, '_blank');
}

export function saveExternalFeatureStatus(status) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/management/featureservice/changeFeatureActivation?featureName=externalUsers&isActive=${status}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  });
}
export function isExternalFeatureActive() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/management/featureservice/isActiveFeature?featureName=externalUsers`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(resp => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting External Feature settings');
    }
  });
}
