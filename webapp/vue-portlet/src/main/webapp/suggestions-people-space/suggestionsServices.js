export function getPeopleSuggestions() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/people/contacts/suggestions`,{credentials: 'include'}).then(resp => resp.json());
}

export function getSpaceSuggestions(){
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/spaces/suggestions`, {credentials: 'include'}).then(resp => resp.json());
}

export function sendConnectionRequest(userID) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/people/contacts/connect/${userID}`, null);
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
  });
}

export function joinSpace(spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/spaces/request/${spaceId}`, null);
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
  });
}
