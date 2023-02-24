export function initCometd(callback) {
  Vue.prototype.$socialWebSocket.initCometd('/SpaceWebNotification');

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