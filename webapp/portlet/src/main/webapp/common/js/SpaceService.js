export function getSpaceTemplates() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaceTemplates/templates?lang=${eXo.env.portal.language}`, {
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

export function getSpaceMembers(query, offset, limit, expand, role, spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/users?role=${role}&q=${query || ''}&offset=${offset || 0}&limit=${limit|| 0}&expand=${expand || ''}&returnSize=true`, {
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
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}?expand=${expand || ''}`, {
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

export function getSpaceByPrettyName(prettyName, expand) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/byPrettyName/${prettyName}?expand=${expand || ''}`, {
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

export function getSpaceByDisplayName(displayName, expand) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/byDisplayName/${displayName}?expand=${expand || ''}`, {
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

export function getSpaceApplications(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/applications`, {
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

export function getSpaceApplicationsChoices() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/applications`, {
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

export function removeSpacesApplication(appName) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/applications/${appName}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function addSpacesApplication(application) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/applications`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      applicationName: application.applicationName,
      contentId: application.contentId,
      description: application.description,
      displayName: application.displayName || application.applicationName,
      id: application.id,
    }),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
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

export function leave(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/spaces/leave/${spaceId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function cancel(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/spaces/cancel/${spaceId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function join(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/spaces/join/${spaceId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function requestJoin(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/spaces/request/${spaceId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function accept(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/spaces/accept/${spaceId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function deny(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/spaces/deny/${spaceId}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function acceptUserRequest(spaceDisplayName, userId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      space: spaceDisplayName,
      user: userId,
      role: 'MEMBER',
      status: 'APPROVED',
    }),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function refuseUserRequest(spaceDisplayName, userId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      space: spaceDisplayName,
      user: userId,
      status: 'IGNORED',
    }),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function cancelInvitation(spaceDisplayName, userId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      space: spaceDisplayName,
      user: userId,
      status: 'IGNORED',
    }),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function promoteManager(spaceDisplayName, userId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      space: spaceDisplayName,
      user: userId,
      role: 'manager',
    }),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function removeManager(spacePrettyName, username) {
  const id = `${spacePrettyName}:${username}:manager`;
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships/${id}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function setAsRedactor(spaceDisplayName, userId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      space: spaceDisplayName,
      user: userId,
      role: 'redactor',
    }),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error(`Error while setting user ${userId} as a redactor in ${spaceDisplayName} space`, resp);
    }
  });
}

export function removeRedactor(spacePrettyName, username) {
  const id = `${spacePrettyName}:${username}:redactor`;
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships/${id}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function addApplication(spaceId, appId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/applications`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: `appId=${appId}`,
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function removeApplication(spaceId, appId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/applications/${appId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function moveApplicationUp(spaceId, appId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/applications/${appId}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: 'transition=-1',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function moveApplicationDown(spaceId, appId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/applications/${appId}`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: 'transition=1',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function removeMember(spacePrettyName, username) {
  const id = `${spacePrettyName}:${username}:member`;
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships/${id}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
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
  const data = {'user': item.username,'space': item.displayName, 'status':'IGNORED'};
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships/`, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
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
