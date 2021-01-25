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
