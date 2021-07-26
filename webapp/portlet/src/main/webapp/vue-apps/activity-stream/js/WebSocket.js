export function initCometd(callback) {
  const loc = window.location;
  const cometdContext = eXo.env.portal.cometdContext;
  const cometdToken = eXo.env.portal.cometdToken;

  cCometd.configure({
    url: `${loc.protocol}//${loc.hostname}${(loc.port && ':') || ''}${loc.port || ''}/${cometdContext}/cometd`,
    exoId: eXo.env.portal.userName,
    exoToken: cometdToken,
  });

  cCometd.subscribe('/eXo/Application/ActivityStream', null, (event) => {
    const data = event.data && JSON.parse(event.data);
    if (!data) {
      return;
    }
    callback(data);
  });
}

export function isDisconnected() {
  return cCometd.isDisconnected();
}