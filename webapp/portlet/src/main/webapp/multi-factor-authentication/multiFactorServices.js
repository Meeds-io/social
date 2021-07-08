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
