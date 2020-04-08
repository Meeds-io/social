export function searchSpacesOrUsers(filter, result, includeUsers, includeSpaces, searchStartedCallback, searchEndCallback) {
  if (includeSpaces) {
    searchStartedCallback('space');
    searchSpaces(filter, result)
      .finally(() => searchEndCallback && searchEndCallback('space'));
  }
  if (includeUsers) {
    searchStartedCallback('organization');
    searchUsers(filter, result)
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

function searchUsers(filter, items) {
  const params = $.param({
    nameToSearch: filter,
    typeOfRelation: 'mention_activity_stream',
    currentUser: eXo.env.portal.userName,
  });
  return fetch(`/portal/rest/social/people/suggest.json?${params}`, {credentials: 'include'})
    .then(resp => resp && resp.ok && resp.json())
    .then(data => {
      if (data) {
        data = data.options || data;
        data.forEach((item) => {
          if (item.id && item.id.indexOf('@') === 0) {
            const username = item.id.substring(1);
            items.push({
              id: `organization:${username}`,
              remoteId: username,
              providerId: 'organization',
              profile: {
                fullName: item.name,
                avatarUrl: item.avatar ? item.avatar : `/portal/rest/v1/social/users/${username}/avatar`,
              },
            });
          }
        });
      }
    });
}
