
export function getUser(username, expand) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${username}?expand=${expand || ''}`, {
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

export function isSuperUser() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users/isSuperUser`, {
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

export function isDelegatedAdministrator() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users/isDelegatedAdministrator`, {
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

export function isSynchronizedUserAllowedToChangePassword() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users/isSynchronizedUserAllowedToChangePassword`, {
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

export function getUserByEmail(email) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/email/${email}`, {
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

export function getUsers(query, offset, limit, expand) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users?q=${query || ''}&offset=${offset || 0}&limit=${limit|| 0}&expand=${expand || ''}&returnSize=true`, {
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

export function getUsersByStatus(query, offset, limit, status) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users?q=${query || ''}&offset=${offset || 0}&limit=${limit|| 0}&status=${status || 'ENABLED'}&returnSize=true`, {
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

export function getConnections(query, offset, limit, expand) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${eXo.env.portal.userName}/connections?q=${query || ''}&offset=${offset || 0}&limit=${limit|| 0}&expand=${expand || ''}&returnSize=true`, {
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

export function getInvitations(offset, limit, expand) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/connections/invitations?offset=${offset || 0}&limit=${limit|| 0}&expand=${expand || ''}&returnSize=true`, {
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

export function getPending(offset, limit, expand) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/connections/pending?offset=${offset || 0}&limit=${limit|| 0}&expand=${expand || ''}&returnSize=true`, {
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

export function getSuggestionsUsers() {
  const cachedSuggestions = sessionStorage && sessionStorage.getItem(`Suggestions_Users_${eXo.env.server.sessionId}`);
  if (cachedSuggestions) {
    return Promise.resolve(JSON.parse(cachedSuggestions));
  }
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/people/contacts/suggestions`,{
    credentials: 'include'
  }).then(resp => {
    if (!resp || !resp.ok) {
      return resp.text().then((text) => {
        throw new Error(text);
      });
    } else {
      return resp.json();
    }
  }).then(data => {
    if (sessionStorage && data) {
      try {
        sessionStorage.setItem(`Suggestions_Users_${eXo.env.server.sessionId}`, JSON.stringify(data));
      } catch (e) {
        // Expected when Quota Error is thrown 
      }
    }
    return data;
  });
}

export function sendConnectionRequest(userID) {
  if (sessionStorage) {
    sessionStorage.removeItem(`Suggestions_Users_${eXo.env.server.sessionId}`);
  }
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/people/contacts/connect/${userID}`,{
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}

export function ignoreSuggestion(receiver) {
  if (sessionStorage) {
    sessionStorage.removeItem(`Suggestions_Users_${eXo.env.server.sessionId}`);
  }
  const sender = eXo.env.portal.userName;
  const data = {'sender': sender ,'receiver': receiver,'status': 'IGNORED'};
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/usersRelationships/`, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    credentials: 'include',
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

export function connect(userId) {
  if (sessionStorage) {
    sessionStorage.removeItem(`Suggestions_Users_${eXo.env.server.sessionId}`);
  }
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/usersRelationships`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      sender: eXo.env.portal.userName,
      receiver: userId,
      status: 'pending',
    }),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function confirm(userId) {
  if (sessionStorage) {
    sessionStorage.removeItem(`Suggestions_Users_${eXo.env.server.sessionId}`);
  }
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/usersRelationships`, {
    method: 'PUT',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      sender: userId,
      receiver: eXo.env.portal.userName,
      status: 'confirmed',
    }),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function deleteRelationship(userId) {
  if (sessionStorage) {
    sessionStorage.removeItem(`Suggestions_Users_${eXo.env.server.sessionId}`);
  }
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/usersRelationships/${eXo.env.portal.userName}/${userId}`, {
    method: 'DELETE',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    } else {
      return resp.json();
    }
  });
}

export function updateProfileField(username, name, value) {
  const formData = new FormData();
  formData.append('name', name);
  formData.append('value', value);

  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${username}`, {
    method: 'PATCH',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then(resp => {
    if (!resp || !resp.ok) {
      return resp.text();
    }
  }).then(error => {
    if (error) {
      throw new Error(error);
    }
  });
}

export function updateProfileFields(username, obj, fields) {
  const objectToSend = {};
  for (const i in fields) {
    objectToSend[fields[i]] = obj[fields[i]];
  }

  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${username}/profile`, {
    method: 'PATCH',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(objectToSend),
  }).then(resp => {
    if (!resp || !resp.ok) {
      return resp.text();
    }
  }).then(error => {
    if (error) {
      throw new Error(error);
    }
  });
}

export function changePassword(username, currentPassword, newPassword) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users/${username}/changePassword`, {
    method: 'PATCH',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: `currentPassword=${currentPassword}&newPassword=${newPassword}`
  }).then(resp => {
    if (!resp || !resp.ok) {
      return resp.text();
    }
  }).then(error => {
    if (error) {
      throw new Error(error);
    }
  });
}

export function importUsers(uploadId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/csv`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: `uploadId=${uploadId}`,
  }).then(resp => {
    if (!resp || !resp.ok) {
      return resp.text();
    }
  }).then(error => {
    if (error) {
      throw new Error(error);
    }
  });
}

export function checkImportUsersProgress(uploadId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/csv`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: `uploadId=${uploadId}&progress=true`,
  }).then(resp => {
    if (!resp || !resp.ok) {
      return resp.text();
    } else {
      return resp.json();
    }
  }).then(data => {
    if (typeof data === 'string') {
      throw new Error(data);
    }
    return data;
  });
}

export function cleanImportUsers(uploadId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/csv`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: `uploadId=${uploadId}&clean=true`,
  }).then(resp => {
    if (!resp || !resp.ok) {
      return resp.text();
    } else {
      return resp.json();
    }
  }).then(data => {
    if (typeof data === 'string') {
      throw new Error(data);
    }
    return data;
  });
}

export function multiSelectAction(action, selectedUsers) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/bulk/${action}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PATCH',
    body: JSON.stringify(selectedUsers),
  }).then((resp) => {
    if (!resp || !resp.ok) {
      throw new Error('Error when updating users');
    } else {
      return resp.json();
    }
  });
}
