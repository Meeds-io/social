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
var UISpaceMemberSuggest = {
	DEFAULT_REST_INFO : {
	  CONTEXT_NAME : 'rest-socialdemo',
	  PATH : '/social/people/suggest.json'
	},
	onLoad : function(params) {
	  var restContextName = params.restContextName || null;
    var currentUserName = params.currentUserName || null;
    var typeOfRelation = params.typeOfRelation || null;
    var spaceURL = params.spaceURL || null;
      
		var suggestEl = $('#user');
		
		$(suggestEl).autosuggest(buildURL(), {multisuggestion:true, defaultVal:""});
		
	  function buildURL() {
	    restContextName = (restContextName) ? restContextName : UISpaceMemberSuggest.DEFAULT_REST_INFO.CONTEXT_NAME;
	    
	    var restURL = "/" + restContextName + UISpaceMemberSuggest.DEFAULT_REST_INFO.PATH;
	          
	    restURL = restURL + '?nameToSearch=input_value';
	
      if (currentUserName) {
        restURL += "&currentUser=" + currentUserName;
      }

      if (spaceURL) {
         restURL += "&spaceURL=" + spaceURL;
      }
      
      if (typeOfRelation) {
	      restURL += "&typeOfRelation=" + typeOfRelation;
	    }
	
	    return restURL;
	  }
	}
};	

return UISpaceMemberSuggest;
})($);
