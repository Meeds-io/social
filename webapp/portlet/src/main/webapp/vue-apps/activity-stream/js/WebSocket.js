export function initCometd(callback, standalone) {
  Vue.prototype.$socialWebSocket.initCometd('/SpaceWebNotification');

  if (!standalone) {
    cCometd.subscribe('/eXo/Application/ActivityStream', null, (event) => {
      const data = event.data && JSON.parse(event.data);
      if (!data) {
        return;
      }
      callback(data);
    });
  }
}

export function isDisconnected() {
  return cCometd.isDisconnected();
}