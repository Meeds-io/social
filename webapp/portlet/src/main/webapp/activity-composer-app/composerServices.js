export function postMessageInSpace(message, activityType, attachments, spaceId, templateParams) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}/activities`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify({
      'title': message,
      'type': activityType,
      'templateParams': templateParams || {},
      'files': attachments
    })
  }).then((data) => {
    return data.json();
  });
}

export function postMessageInUserStream(message, activityType, attachments, userName, templateParams) {
  const params = templateParams || {};
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${userName}/activities`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify({
      'title': message,
      'type': activityType,
      'templateParams': params,
      'files': attachments
    })
  }).then((data) => {
    return data.json();
  });
}
export function updateActivityInUserStream(message, activityId, activityType, attachments, templateParams) {
  const params = Object.assign({
    'MESSAGE': message
  }, templateParams || {});
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/activities/${activityId}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: JSON.stringify({
      'updateDate': Date.now(),
      'title': message,
      'templateParams': params,
      'type': activityType,
      'files': attachments,
    })
  }).then((data) => {
    return data.json();
  });
}