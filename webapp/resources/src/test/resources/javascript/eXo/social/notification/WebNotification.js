/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
(function($){
  var WebNotification = {
      plugins : [],
      register : function(plugin) {
        WebNotification.plugins.push(plugin);
        if ($.isFunction(plugin.init)) {
          plugin.init();
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
          WebNotification.ajaxRequest(restURL, function(data) {
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
            window.open(url, "_self");
          }, 500);
        }
        return me;
      },
      ajaxRequest : function (url, callBack) {
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