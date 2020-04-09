export function getPeopleSuggestions() {
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

export function getSpaceSuggestions(){
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

export function ignoredSuggestionConnection(sender, receiver) {
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

export function joinSpace(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/spaces/request/${spaceId}`, {
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

export function ignoredSuggestionSpace(item) {
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
