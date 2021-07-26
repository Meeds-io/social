export function getExternalSpacesList() {
  return fetch(`${Vue.prototype.$spacesConstants.SOCIAL_USER_API}${eXo.env.portal.userName}/spaces?limit=-1`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when getting spaces of current user');
    }
  });
}

export function getExternalSpacesRequests() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships?status=invited&limit=-1`, {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when getting external spaces requests');
    }
  });
}


export function replyInvitationToJoinSpace(spaceMembershipId, reply) {
  const data = {status: `${reply}`};
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spacesMemberships/${spaceMembershipId}`, {
    method: 'PUT',
    credentials: 'include',
    body: JSON.stringify(data),
    headers: {
      'Content-Type': 'application/json'
    }
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    }
    else {
      throw new Error ('Error when replying invitation to join space');
    }
  });
}
