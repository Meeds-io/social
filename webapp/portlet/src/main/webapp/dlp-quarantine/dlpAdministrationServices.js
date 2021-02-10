export function saveDlpFeatureStatus(status) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/management/featureservice/changeFeatureActivation?featureName=dlp&isActive=${status}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  });
}

export function isDlpFeatureActive() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/management/featureservice/isActiveFeature?featureName=dlp`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then(resp => {
    if(resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting dlp Feature status');
    }
  });
}

export function getDlpPositiveItems(offset, itemsPerPage) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/dlp/items?offset=${offset || 0}&limit=${itemsPerPage}`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error retrieving list of dlp positive items');
    }
  });
} 

export function getDlpKeywords() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/dlp/items/keywords`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.text();
    } else {
      throw new Error('Error retrieving dlp keywords');
    }
  });
}

export function setDlpKeywords(keywords) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/dlp/items/keywords`, {
    method: 'POST',
    credentials: 'include',
    body: keywords
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Error setting dlp keywords', resp);
    } else {
      return resp.json();
    }
  });
} 

export function getDlpPermissions() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/dlp/items/permissions`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error retrieving dlp permissions');
    }
  });
}

export function checkIsAdminMemberGroup() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/dlp/items/permissions/isAdministrator`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error checking is admin member group');
    }
  });
}

export async function getPermissionsData(query) {
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
    throw new Error('Unable to get permissions data');
  }
}

export function saveDlpPermissions(newPermissions) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/dlp/items/save/permissions`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: newPermissions,
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Unable to save dlp permissions');
    } else {
      return resp.json();
    }
  });
}