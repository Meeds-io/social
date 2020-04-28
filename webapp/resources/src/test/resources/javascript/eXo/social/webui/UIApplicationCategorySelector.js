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
(function($, Selectors) { 
var UIApplicationCategorySelector = {
  init: function(params) {
	  var applicationCategoryIds = (params.applicationCategoryIds.replace('[','').replace(']','')).split(',');
	  var allApplicationCategorySize = parseInt(params.allApplicationCategorySize);
	
    if(applicationCategoryIds != null) {
	    for ( var i = 0; i < allApplicationCategorySize; i++ ) {
        $('#' + applicationCategoryIds[i]).on('mouseover', function(evt){
          Selectors.UIItemSelector.onOver(this, true);
        });

        $("#" + applicationCategoryIds[i]).on('mouseout', function(evt){
          Selectors.UIUserSelector.onOver(this, false);
        });
	    }
    }
  }
};

return UIApplicationCategorySelector;
})($, selector);