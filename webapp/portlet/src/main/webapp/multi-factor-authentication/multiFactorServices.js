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

<<<<<<< HEAD
export function updateRevocationRequest(id, status) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/mfa/revocations/${id}?status=${status}`, {
    method: 'PUT',
    credentials: 'include',
  });
}


export function getRevocationRequests() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/mfa/revocations`, {
=======
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
>>>>>>> 650fa394a7 (Task-46503 : AdminMFA Activating and choosing MFA system)
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
<<<<<<< HEAD
      return resp.json();
    } else {
      throw new Error('Error when getting mfa revocation requests');
    }
  });
}
=======
      return resp.text();
    } else {
      throw new Error('Error when getting current MFA system');
    }
  });
}

>>>>>>> 650fa394a7 (Task-46503 : AdminMFA Activating and choosing MFA system)
