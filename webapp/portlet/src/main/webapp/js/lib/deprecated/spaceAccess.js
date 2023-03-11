(function() {
  window.ajaxGet = function(url, callback) {
    return fetch(url, {
      method: 'GET',
      credentials: 'include',
    }).then(resp => resp && resp.ok && callback && callback());
  };
})();