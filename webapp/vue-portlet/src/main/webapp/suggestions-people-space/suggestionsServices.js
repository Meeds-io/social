export function getPeopleSuggestions() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/people/contacts/suggestions`,{credentials: 'include'}).then(resp => resp.json());
}

export function getSpaceSuggestions(){
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/homepage/intranet/spaces/suggestions`, {credentials: 'include'}).then(resp => resp.json());
}