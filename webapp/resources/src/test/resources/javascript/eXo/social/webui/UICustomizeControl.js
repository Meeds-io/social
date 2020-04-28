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
(function($, uiForm) { 
	var UICustomizeControl = {
	  onLoad : function(uicomponentId) {
			$('#' + uicomponentId).find('input[type=radio]').
				on('click', function() {
					var uiForm = $(this).parents('.UIForm:first');
					if (uiForm.length > 0) {
						var action = uiForm.find('.uiAction:first').find('a:first');
						var link = String(action.attr('onclick')).replace('Create', 'ChangeOption');
						eval(link);			
					}				
				});
			
	
	  }
	};

	return UICustomizeControl;
})($, uiForm);