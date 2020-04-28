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
  var UISpaceSearch = {
    init : function(params) {
      UISpaceSearch.typeOfSuggest = params.typeOfSuggest || '';
      UISpaceSearch.typeOfRelation = params.typeOfRelation || '';
      UISpaceSearch.spaceURL = params.spaceURL || '';
      UISpaceSearch.profileSearch = $("#" + params.uicomponentId);
      UISpaceSearch.searchBtn = UISpaceSearch.profileSearch.find('#SearchButton');
      var searchEl = UISpaceSearch.profileSearch.find('#SpaceSearch');
      //    
      $(searchEl).keypress(function(event) {
        var e = event || window.event;
        var keynum = e.keyCode || e.which;  
        if(keynum == 13) {
          UISpaceSearch.searchBtn.click();     
          event.stopPropagation();
        }
      });
      
      $(searchEl).autosuggest(buildURL(), {
        defaultVal : ''
      });

      //
      function buildURL() {
        var restURL = "/" + eXo.social.portal.rest + eXo.social.portal.context + '/social/spaces/suggest.json?conditionToSearch=input_value';
        //
        var userName = eXo.social.portal.userName;
        if (userName && userName.length > 0) {
          restURL += "&currentUser=" + userName;
        }
        if (UISpaceSearch.typeOfRelation && UISpaceSearch.typeOfRelation.length > 0) {
          restURL += "&typeOfRelation=" + UISpaceSearch.typeOfRelation;
        }
        if (UISpaceSearch.spaceURL && UISpaceSearch.spaceURL.length > 0) {
          if (UISpaceSearch.typeOfSuggest == 'people') {
            restURL += "&spaceURL=" + UISpaceSearch.spaceURL;
          }
        }
        return restURL;
      }
    }
};

return UISpaceSearch;
})($);

