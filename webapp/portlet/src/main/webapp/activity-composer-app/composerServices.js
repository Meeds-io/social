export function postMessageInSpace(message, activityType, attachments, spaceId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/activities`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify({
      'title': message,
      'type': activityType,
      'templateParams': {},
      'files': attachments
    })
  }).then((data) => {
    return data.json();
  });
}

export function postMessageInUserStream(message, activityType, attachments, userName) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${userName}/activities`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify({
      'title': message,
      'type': activityType,
      'templateParams': {},
      'files': attachments
    })
  }).then((data) => {
    return data.json();
  });
}