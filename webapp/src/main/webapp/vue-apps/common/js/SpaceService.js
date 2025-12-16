const spaces = {};

export function getSpaceTemplates() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaceTemplates/templates`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function getSpacesCountByTemplates() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/countByTemplate`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function findSpaceExternalInvitationsBySpaceId(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/externalInvitations`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function declineExternalInvitation(spaceId, invitationId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/externalInvitations/${invitationId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function getSpaceById(spaceId, expand, forceRefresh) {
  expand = expand || '';
  const key = `${spaceId}-${expand}`;
  if (spaces[key] && !forceRefresh) {
    return Promise.resolve(spaces[key]);
  }
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}?expand=${expand}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  }).then(space => {
    if (space) {
      spaces[key] = space;
    }
    return space;
  });
}

export function getSpaceByPrettyName(prettyName, expand) {
  expand = expand || '';
  const key = `${prettyName}-${expand}`;
  if (spaces[key]) {
    return Promise.resolve(spaces[key]);
  }
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/byPrettyName/${prettyName}?expand=${expand}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  }).then(space => {
    if (space) {
      spaces[key] = space;
    }
    return space;
  });
}

export function getSpaceByGroupId(groupId, expand) {
  expand = expand || '';
  const groupSuffix = groupId.replace('/spaces/', '');
  return getSpaceByGroupSuffix(groupSuffix, expand);
}

export function getSpaceByGroupSuffix(groupSuffix, expand) {
  expand = expand || '';
  const key = `group-${groupSuffix}-${expand}`;
  if (spaces[key]) {
    return Promise.resolve(spaces[key]);
  }
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/byGroupSuffix/${groupSuffix}?expand=${expand}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Response code indicates a server error', resp);
    }
  }).then(space => {
    if (space) {
      spaces[key] = space;
    }
    return space;
  });
}

export function getSpaces(query, offset, limit, filter, expand, templateId, sortBy, sortDirection) {
  return getSpacesByFilter({
    query,
    offset,
    limit,
    filter,
    expand,
    templateId,
    sortBy,
    sortDirection,
  });
}

export function getSpacesByFilter(options) {
  const formData = new FormData();
  if (options.expand) {
    formData.append('expand', options.expand);
  } else {
    formData.append('expand', options.filter === 'requests' ? 'pending' : options.limit && 'managers' || '');
  }
  if (options.query) {
    formData.append('q', options.query);
  }
  if (options.templateId) {
    if (typeof options.templateId === 'object' && options.templateId.length) {
      options.templateId.forEach(id => formData.append('templateId', id));
    } else {
      formData.append('templateId', options.templateId);
    }
  }

  if (options.subTemplateId) {
    if (typeof options.subTemplateId === 'object' && options.subTemplateId.length) {
      options.subTemplateId.forEach(id => formData.append('subTemplateId', id));
    } else {
      formData.append('subTemplateId', options.subTemplateId);
    }
  }

  if (options.categoryIds?.length) {
    options.categoryIds.forEach(c => formData.append('categoryId', c));
  }

  if (options.excludedCategoryIds?.length) {
    options.excludedCategoryIds.forEach(c => formData.append('excludedCategoryId', c));
  }
  if (options.registration) {
    formData.append('registration', options.registration.toUpperCase());
  }
  if (options.visibility) {
    formData.append('visibility', options.visibility.toUpperCase());
  }
  if (options.offset) {
    formData.append('offset', options.offset);
  }
  if (options.limit) {
    formData.append('limit', options.limit);
  }
  if (options.filter) {
    formData.append('filterType', options.filter);
  }
  if (options.parentSpaceId) {
    formData.append('parentSpaceId', options.parentSpaceId);
  }
  if (options.onlyParentSpaces) {
    formData.append('onlyParentSpaces', options.onlyParentSpaces);
  }
  if (options.sortBy) {
    formData.append('sort', options.sortBy);
  }
  if (options.sortDirection) {
    formData.append('order', options.sortDirection);
  }
  if (options.token) {
    formData.append('token', options.token);
  }
  if (options.excludedIds?.length) {
    options.excludedIds.forEach(id => formData.append('excludedId', id));
  }
  formData.append('returnSize', true);
  const urlParams = new URLSearchParams(formData).toString();
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces?${urlParams}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function removeSpace(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function updateSpace(space) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${space.id}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(space),
  }).then(resp => {
    if (!resp || !resp.ok) {
      return resp.text().then((text) => {
        throw new Error(text);
      });
    } else {
      return resp.json();
    }
  });
}

export function createSpace(space) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(space),
  }).then(resp => {
    if (!resp || !resp.ok) {
      return resp.text().then((text) => {
        throw new Error(text);
      });
    } else {
      return resp.json();
    }
  });
}

export function prepareSpaceInstance(space) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/prepare`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(space),
  }).then(resp => {
    if (!resp?.ok) {
      return resp.text().then((text) => {
        throw new Error(text);
      });
    } else {
      return resp.text();
    }
  });
}

export function getSpaceMemberships(params) {
  const formData = new FormData();
  formData.append('offset', params.offset || 0);
  formData.append('limit', params.limit || 0);
  if (params.user) {
    formData.append('user', params.user);
  }
  if (params.space) {
    formData.append('space', params.space);
  }
  if (params.status) {
    formData.append('status', params.status || 'member');
  }
  if (params.query) {
    formData.append('query', params.query);
  }
  if (params.expand) {
    formData.append('expand', params.expand);
  }
  if (params.returnSize) {
    formData.append('returnSize', params.returnSize);
  }
  const urlParams = new URLSearchParams(formData).toString();
  return fetch( `/portal/rest/v1/social/spacesMemberships?${urlParams}`, {
    method: 'GET',
    credentials: 'include',
    signal: params.signal,
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    }  else {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function isSpaceMember(spaceId, userId) {
  return getSpaceMemberships({
    offset: 0,
    limit: 1,
    user: userId,
    space: spaceId,
    status: 'member',
  }).then(data => !!data?.spacesMemberships?.length);
}

export function leave(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      space: spaceId,
      user: eXo.env.portal.userName,
      role: 'member',
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function cancel(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      space: spaceId,
      user: eXo.env.portal.userName,
      status: 'ignored',
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function join(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      space: spaceId,
      user: eXo.env.portal.userName,
      role: 'member',
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function requestJoin(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      space: spaceId,
      user: eXo.env.portal.userName,
      status: 'pending',
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function accept(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      space: spaceId,
      user: eXo.env.portal.userName,
      status: 'approved',
    }),
  }).then((resp) => {
    if (!resp?.ok) {
      throw new Error ('Error when replying invitation to join space');
    }
  });
}

export function deny(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      space: spaceId,
      user: eXo.env.portal.userName,
      status: 'ignored',
    }),
  }).then((resp) => {
    if (!resp?.ok) {
      throw new Error ('Error when replying invitation to join space');
    }
  });
}

export function acceptUserRequest(spaceId, username) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      space: spaceId,
      user: username,
      role: 'MEMBER',
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function refuseUserRequest(spaceId, username) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      space: spaceId,
      user: username,
      status: 'ignored',
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function cancelInvitation(spaceId, username) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      space: spaceId,
      user: username,
      status: 'ignored',
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function promoteManager(spaceId, username) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      space: spaceId,
      user: username,
      role: 'manager',
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function removeManager(spaceId, username) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      space: spaceId,
      user: username,
      role: 'manager',
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function promoteRedactor(spaceId, username) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      space: spaceId,
      user: username,
      role: 'redactor',
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error(`Error while setting user ${username} as a redactor in ${spaceId} space`, resp);
    }
  });
}

export function removeRedactor(spaceId, username) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      space: spaceId,
      user: username,
      role: 'redactor',
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function promotePublisher(spaceId, username) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      space: spaceId,
      user: username,
      role: 'publisher',
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function removePublisher(spaceId, username) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      space: spaceId,
      user: username,
      role: 'publisher',
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function removeMember(spaceId, username) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'DELETE',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      space: spaceId,
      user: username,
      role: 'member',
    }),
  }).then(resp => {
    if (!resp?.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function getSuggestionsSpace(){
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/spaces/suggestions`, {
    credentials: 'include'
  }).then(resp => {
    if (!resp || !resp.ok) {
      return resp.text().then((text) => {
        throw new Error(text);
      });
    } else {
      return resp.json();
    }
  });
}

export function ignoreSuggestion(item) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      space: item.id,
      user: item.username,
      status: 'ignored',
    }),
  }).then(resp => {
    if (!resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function checkExternals(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/checkExternals`, {
    method: 'GET',
    credentials: 'include'
  }).then(resp => {
    if (!resp || !resp.ok) {
      return resp.text().then((text) => {
        throw new Error(text);
      });
    } else {
      return resp.text();
    }
  });
}

export function markAsRead(spaceId, applicationName, applicationItemId, userEvent) {
  cCometd.publish('/SpaceWebNotification', JSON.stringify({
    wsEventName: 'notification.read.item',
    message: {
      spaceWebNotificationItem: JSON.stringify({
        userId: eXo.env.portal.userIdentityId,
        spaceId,
        applicationName,
        applicationItemId,
        userEvent,
      })
    }
  }));
}

export function markAsUnread(spaceId, applicationName, applicationItemId, userEvent) {
  cCometd.publish('/SpaceWebNotification', JSON.stringify({
    wsEventName: 'notification.unread.item',
    message: {
      spaceWebNotificationItem: JSON.stringify({
        userId: eXo.env.portal.userIdentityId,
        spaceId,
        applicationName,
        applicationItemId,
        userEvent,
      })
    }
  }));
}

export function markAllAsRead(spaceId) {
  cCometd.publish('/SpaceWebNotification', JSON.stringify({
    wsEventName: 'notification.read.allItems',
    message: {
      spaceWebNotificationItem: JSON.stringify({
        userId: eXo.env.portal.userIdentityId,
        spaceId: spaceId || 0,
        userEvent: 'click',
      })
    }
  }));
}

export function muteSpace(spaceId, unmute) {
  return fetch(`/portal/rest/notifications/settings/${eXo.env.portal.userName}/spaces/${spaceId}`, {
    method: 'PATCH',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: `enable=${unmute || false}`,
  }).then((resp) => {
    if (!resp.ok) {
      throw new Error('Error processing request on server');
    }
    if (!window.MUTED_SPACES) {
      window.MUTED_SPACES = {};
    }
    if (unmute) {
      window.MUTED_SPACES[spaceId] = false;
    } else {
      window.MUTED_SPACES[spaceId] = true;
    }
  });
}
