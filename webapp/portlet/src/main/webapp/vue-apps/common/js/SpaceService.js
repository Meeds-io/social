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

export function getSpaceMembers(query, offset, limit, expand, role, spaceId, signal) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/users?role=${role}&q=${query || ''}&offset=${offset || 0}&limit=${limit|| 0}&expand=${expand || ''}&returnSize=true`, {
    method: 'GET',
    credentials: 'include',
    signal: signal
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
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

export function isSpaceMember(spaceId, userId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/users/${userId}`, {
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

export function getSpaceById(spaceId, expand) {
  expand = expand || '';
  const key = `${spaceId}-${expand}`;
  if (spaces[key]) {
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

export function getSpaceByDisplayName(displayName, expand) {
  expand = expand || '';
  const key = `${displayName}-${expand}`;
  if (spaces[key]) {
    return Promise.resolve(spaces[key]);
  }
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/byDisplayName/${displayName}?expand=${expand}`, {
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

export function restoreSpaceHomeLayout(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/layout/home/${spaceId}`, {
    credentials: 'include',
    method: 'PUT',
  });
}

export function getSpaceNavigations(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/navigations`, {
    method: 'GET',
    credentials: 'include',
  })
    .then(resp => {
      if (!resp || !resp.ok) {
        throw new Error('Response code indicates a server error', resp);
      } else {
        return resp.json();
      }
    })
    .then(data => {
      data = data || [];
      if (data.length && data[0].children && data[0].children.length) {
        data = [data[0], ...data[0].children];
      }
      return data;
    });
}

export function getSpaces(query, offset, limit, filter, expand) {
  if (!expand) {
    expand = filter === 'requests' ? 'pending' : limit && 'managers' || '';
  }
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces?q=${query || ''}&offset=${offset || 0}&limit=${limit|| 0}&filterType=${filter}&returnSize=true&expand=${expand}`, {
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

export function getSpaceMemberships(params) {
  const formData = new FormData();
  formData.append('user', params.user);
  formData.append('offset', params.offset || 0);
  formData.append('limit', params.limit || 0);
  formData.append('returnSize', params.returnSize !== false);
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
  const urlParams = new URLSearchParams(formData).toString();
  return fetch( `/portal/rest/v1/social/spacesMemberships?${urlParams}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp?.ok) {
      return resp.json();
    }  else {
      throw new Error('Response code indicates a server error', resp);
    }
  });
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

export function shareActivityOnSpaces(spaceId, sharedActivity) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities/${spaceId}/share`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(sharedActivity),
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
