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
(function($) { 
var UIProfile = {
  labels: {},
  KEYS : {
    ENTER : 13
  },
  init: function(params) {
    var positionId = params.positionId || null;
    var saveButtonId = params.saveButtonId || null;

    var positionEl = $("#" + positionId);
    var saveButtonEl = $("#" + saveButtonId);

    if (positionEl.length > 0 && saveButtonEl.length > 0) {
      positionEl.on('keydown', function(event) {
        if ((event.keyCode || event.which) == UIProfile.KEYS.ENTER) {
          saveButtonEl.click();
          event.preventDefault();
          return;
        }
      });
    }
  },
  initUserProfilePopup : function(uicomponentId, labels) {
    //
    labels = labels || UIProfile.labels;
    UIProfile.labels = labels;
    
    // User Profile Popup initialize
	  var portal = eXo.social.portal;
	  var restUrl = window.location.origin + portal.context + '/' + portal.rest + '/social/people' + '/getPeopleInfo/{0}.json';
    
    var container = $('#' + uicomponentId).closest('.PORTLET-FRAGMENT');
    var userLinks = $(container).find('a[href*="/profile/"]');
    $.each(userLinks, function (idx, el) {
        var userUrl = $(el).attr('href');
        var userId = userUrl.substring(userUrl.lastIndexOf('/') + 1);
        
        $(el).userPopup({
          restURL: restUrl,
          labels: labels,
          content: false,
          defaultPosition: "left",
          keepAlive: true,
          maxWidth: "240px"
        });
    });
  },
  clearUserProfilePopup : function() {
    $('div#socialUsersData').stop().animate({
        'cursor':'none'
    }, 1000, function () {
        $(this).data("CacheSearch", {});
    });
  },
  addLabelToCheckBoxes: function(uicomponentId, label) {
    var component = $('#' + uicomponentId);
    var checkBoxes = component.find('span.uiCheckbox');
    $.each(checkBoxes, function(idx, checkBox) {
      $(checkBox).find('span').text(label);
    });
  }
};

return UIProfile;
})($);