export function getNotifications() {
  return fetch('/portal/rest/notifications/webNotifications', {
    method: 'GET',
    credentials: 'include',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error when getting notification list');
    }
  });
}

export function updateNotification(id, operation) {
  return fetch(`/portal/rest/notifications/webNotifications/${id}`, {
    headers: {
      'Content-Type': 'text/plain',
      credentials: 'include',
    },
    method: 'PATCH',
    body: operation
  });
}

export function initCometd() {
  const loc = window.location;
  cCometd.configure({
    url: `${loc.protocol}//${loc.hostname}${loc.port && ':' || ''}${loc.port || ''}/cometd/cometd`,
    exoId: eXo.env.portal.userName,
    exoToken: eXo.env.portal.cometdToken,
  });

  cCometd.subscribe('/eXo/Application/web/NotificationMessage', null, (event) => {
    const data = JSON.parse(event.data);
    document.dispatchEvent(new CustomEvent('cometdNotifEvent', {'detail': {'data': data}}));
  });
}