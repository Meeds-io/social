export function getSuggestionsUsers() {
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
  });
}

export function sendConnectionRequest(userID) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/people/contacts/connect/${userID}`, {
    method: 'GET',
    credentials: 'include',
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

export function ignoreSuggestion(sender, receiver) {
  const data = {'sender': sender,'receiver': receiver,'status':'IGNORED'};
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/usersRelationships/`, {
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

