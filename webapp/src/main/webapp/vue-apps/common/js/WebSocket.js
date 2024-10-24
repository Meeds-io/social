const subscribedChannels = {};

export function initCometd(channelName, forceSubscription) {
  const loc = window.location;
  const cometdContext = eXo.env.portal.cometdContext;
  const cometdToken = eXo.env.portal.cometdToken;
  cCometd.configure({
    url: `${loc.protocol}//${loc.hostname}${(loc.port && ':') || ''}${loc.port || ''}/${cometdContext}/cometd`,
    exoId: eXo.env.portal.userName,
    exoToken: cometdToken,
  });

  if (forceSubscription || !subscribedChannels[channelName]) {
    subscribedChannels[channelName] = true;
    cCometd.subscribe(channelName, null, (event) => {
      const data = event.data && JSON.parse(event.data);
      if (!data) {
        return;
      }
      const wsMessage = JSON.parse(event.data);
      document.dispatchEvent(new CustomEvent(wsMessage.wsEventName, {detail: wsMessage}));
    });
  }
}
