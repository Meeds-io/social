(function($){
  var WebNotification = {
      plugins : [],
      register : function(plugin) {
        WebNotification.plugins.push(plugin);
        if ($.isFunction(plugin.init)) {
          setTimeout(plugin.init, 800);
        }
      },
      evalMethod : function(methodName, args) {
        var plugins = WebNotification.plugins;
        for (var i = 0; i < plugins.length; ++i) {
          var method = plugins[i][methodName];
          if ($.isFunction(method)) {
            method.apply(this, new Array(args));
          }
        }
      },
      appendMessage : function(message) {
        WebNotification.evalMethod('appendMessage', message);
      },
      doAction : function(elm) {
        WebNotification.evalMethod('doAction', elm);
      },
      doCancelAction : function(id, restURL) {
        if (restURL && restURL.length > 0) {
          WebNotification.ajaxReq(restURL, function(data) {
            WebNotification.evalMethod('doCancelAction', {
              data : data,
              id : id
            });
          });
        }
      },
      markAllRead : function() {
        WebNotification.evalMethod('markAllRead', window);
        return WebNotification;
      },
      markItemRead : function(item) {
        WebNotification.evalMethod('markItemRead', item);
        return WebNotification;
      },
      //Utils
      openURL : function (url) {
        var me = WebNotification;
        if(url && url.length > 0) {
          me.T = setTimeout(function() {
            clearTimeout(me.T);
            $(window).on('hashchange',function(){
              // If only the hash has changed in the url, the page won't be reloaded by window.location.assign.
              // This makes sure the page will be reloaded even if only the hash has changed in the url.
              window.location.reload();
            });
            window.location.assign(url);
          }, 500);
        }
        return me;
      },
      ajaxReq : function (url, callBack) {
        if(url && url.length > 0) {
          $.ajax(url).done(function(data) {
            if($.isFunction(callBack)) {
              callBack(data);
            }
          });
        }
        return WebNotification;
      },
      removeElm : function(elm, callBack) {
        elm.css('overflow', 'hidden').animate({
          height : '0px'
        }, 300, function() {
          if($.isFunction(callBack)) {
            callBack($(this));
          }
          $(this).remove();
        });
        return WebNotification;
      },
      showElm : function(elm) {
        elm.css({'visibility':'hidden', 'overflow':'hidden'}).show();
        var h = elm.height();
        elm.css({'height' : '0px', 'visibility':'visible'}).animate({ 'height' : h + 'px' }, 300, function() {
          $(this).css({'height':'', 'overflow':''});
        });
        return elm;
      }
  };
  return WebNotification;
})(gj);