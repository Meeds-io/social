export async function getGroups(query) {
  try {
    const response = await fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups?q=${query}`, {
      headers: {
        'Content-Type': 'application/json'
      },
      method: 'GET'
    });
    if (response.ok) {
      return response.json();
    } else {
      return response.json().then(error => {
        throw new Error(error.errorCode);
      });
    }
  } catch (e) {
    throw new Error('Unable to get groups data');
  }
}

export function changeMfaFeatureActivation(status) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/mfa/changeMfaFeatureActivation/${status}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: JSON.stringify(status)
  });
}

export function updateRevocationRequest(id, status) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/mfa/revocations/${id}?status=${status}`, {
    method: 'PUT',
    credentials: 'include',
  });
}


export function getRevocationRequests() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/mfa/revocations`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting mfa revocation requests');
    }
  });
}
export function changeMfaSytem(mfaSystem) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/mfa/changeMfaSystem/${mfaSystem}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: (mfaSystem)
  });
}

export function getCurrentMfaSystem() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/mfa/settings`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting current MFA system');
    }
  });
}

export function getAvailableMfaSystem() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/mfa/available`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting available MFA systems');
    }
  });
}

export function getProtectedGroups() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/mfa/getProtectedGroups`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting protected MFA groups');
    }
  });
}

export function getProtectedNavigations() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/mfa/getProtectedNavigations`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting protected MFA navigations');
    }
  });
}

export function saveProtectedGroups(groups) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/mfa/saveProtectedGroups`, {
    method: 'POST',
    credentials: 'include',
    body: groups
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Error when saving MFA protected groups', resp);
    } else {
      return resp.json();
    }
  });

}

export function getNavigations() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/navigations`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting existing navigations');
    }
  });
}

