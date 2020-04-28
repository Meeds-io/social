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
(function(sUtils, $) {
  window.jq=$;
  var Notification = {
    formData: '',
    parentId: '#userNotification',
    mailChannel: 'MAIL_CHANNEL',
    webChannel: 'WEB_CHANNEL',
    onload : function() {
      Notification.formData = $(document.forms['uiNotificationSetting']).serialize();
      Notification.updateUIGrid();
      //
      var parent = $(Notification.parentId);
      //
      parent.find("button#Reset").on('click', function(e) {
        var elm = $(this);
        var close = elm.parents('div:first').attr('data-close');
        var confTitle = elm.parents('div:first').attr('data-conf');
        var actions = {
          action: function() {
            $(Notification.parentId).jzAjax({        
              url : "UserNotificationSetting.resetSetting()",
              data : {},
              success : function(data) {
                var content = $('<div></div>').html(data).find('div.uiUserNotificationPortlet:first').html();
                $(Notification.parentId).html(content);
                Notification.onload();
              }
            }).fail(function(jqXHR, textStatus) {
              alert( "Request failed: " + textStatus + ". "+jqXHR);
            });
          }, 
          label: elm.attr('data-confirm-label')
        };
        sUtils.PopupConfirmation.confirm(elm.attr('id'), [actions], confTitle, elm.attr('data-confirm'), close);
      });
      //
      var horizontal = parent.find('div.form-horizontal');
      //
      horizontal.find('input.iphoneStyle').iphoneStyle({
        disabledClass: 'switchBtnDisabled',
        containerClass: 'uiSwitchBtn',
        labelOnClass: 'switchBtnLabelOn',
        labelOffClass: 'switchBtnLabelOff',
        handleClass: 'switchBtnHandle',
        handleCenterClass: 'switchBtnHandleCenter',
        handleRightClass: 'switchBtnHandleRight',
        checkedLabel: locale.resolve("YES"), 
        uncheckedLabel: locale.resolve("NO"),
        onChange : function() {
          var input = $(this.elem);
          Notification.switchStatus(input.attr('name'), input.hasClass("staus-false"));
        }
      });
    },
    updateUIGrid : function(parent) {
      if(parent == undefined || parent == null) {
        parent = $(Notification.parentId);
      }
      //
      parent.find("button.save-setting").on('click', Notification.saveSetting) ;
      //
      parent.find("a.edit-setting").on('click', function(evt) {
        $(this).parents('div.channel-container:first').removeClass('view').addClass('edit');
      });
    },
    saveSetting : function(e) {
      var jElm = $(this);
      var pluginId = jElm.attr('id');
      var msgOk = jElm.attr('data-ok');
      var msgNOk = jElm.attr('data-nok');
      //
      var parent = jElm.parents('div.channel-container:first');
      var digest = parent.find('select:first').val();
      var channels = "";
      parent.find('input').each(function() {
        if(channels.length > 0) {
          channels += '&';
        }
        channels += $(this).data('channel') + '=' + $(this).is(':checked');
      });
      $(Notification.parentId).jzAjax({        
        url : "UserNotificationSetting.saveSetting()",
        data : {
          "pluginId" : pluginId,
          "channels" : channels,
          "digest" : digest
        },
        success : function(data) {
          if(data.ok == 'true') {
            var td = parent.parents('td:first');
            td.html(data.context);
            Notification.updateUIGrid(td);
          } else {
            if(console.error) {
              console.error(msgNOk);
            }
          }
        }
      }).fail(function(jqXHR, textStatus) {
        alert( "Request failed: " + textStatus + ". "+jqXHR);
      });
    },
    switchStatus : function(channelId, isEnable) {
      $(Notification.parentId).jzAjax({   
  	    url : "UserNotificationSetting.saveActiveStatus()",
  	    data : {
  	      "type": "POST",
  	      "channelId" : channelId.replace('channel', ''),
  	      "enable" : isEnable
  	    },
  	    success : function(data) {
          var parent = $(Notification.parentId);
          var action = parent.find('input[name=channel' + data.type + ']');
          var clazz = "enable", disabled = false;
          if((data.enable == 'true')) {
            action.attr('checked', 'checked');
          } else {
            action.removeAttr('checked');
            clazz = "disabled";
            disabled = true;
          }
          action.attr('class', 'iphoneStyle yesno staus-' + data.enable);
          
          var context = $('<div></div>').html(data.context);
          var grid = context.find('table.uiGrid:first');
          parent.find('table.uiGrid:first').remove();
          grid.insertAfter(parent.find('div.channel-actives:first'));
          //
          Notification.updateUIGrid();
          //
          if (data.type == Notification.webChannel) {
            var intranetNotif = $('#UINotificationPopoverToolbarPortlet');
            if (data.enable == 'true') {
    	    	  intranetNotif.show();
    		    } else {
    		      intranetNotif.hide();
    		    }
          }
  	    }
  	  }).fail(function(jqXHR, textStatus) {
  	    alert("Request failed: " + textStatus + ". " + jqXHR);
  	  });
  	}
  };
  Notification.onload();
  return Notification;
})(socialUtil, jQuery);
