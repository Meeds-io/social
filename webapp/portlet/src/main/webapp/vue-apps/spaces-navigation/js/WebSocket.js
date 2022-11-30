export function initCometd() {
  const loc = window.location;
  const cometdContext = eXo.env.portal.cometdContext;
  const cometdToken = eXo.env.portal.cometdToken;
  cCometd.configure({
    url: `${loc.protocol}//${loc.hostname}${(loc.port && ':') || ''}${loc.port || ''}/${cometdContext}/cometd`,
    exoId: eXo.env.portal.userName,
    exoToken: cometdToken,
  });

  cCometd.subscribe('/spaceWebNotification/unread', null, (event) => {
    const data = event.data && JSON.parse(event.data);
    if (!data) {
      return;
    }
    document.dispatchEvent(new CustomEvent('social-spaces-notification-updated', {detail: data}));
  });
}