export function getReminderStatus(reminderName) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/changes/reminder/${reminderName}`, {
    method: 'GET',
    credentials: 'include',
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error(`Response code indicates a server error ${resp}`);
    } else {
      return resp.text();
    }
  }).then(userFlagStatus => userFlagStatus === 'true');
}

export function markReminderAsRead(reminderName) {
  return fetch(`${Vue.prototype.$spacesConstants.PORTAL_CONTEXT}/${Vue.prototype.$spacesConstants.PORTAL_REST}/changes/reminder/${reminderName}`, {
    method: 'PATCH',
    credentials: 'include',
  }).then((resp) => {
    if (!resp || !resp.ok) {
      throw new Error('Error update reminder status');
    }
  });
}