export function changeFeatureActivation(status) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/dlp/changeFeatureActivation/${status}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: JSON.stringify(status)
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
