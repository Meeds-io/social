/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

(function() {
	eXo.i18n.I18NMessage = {
	
	  /**
	   * Get message from resource bundle files, this function often is called from .js files
	   * @param {String} str know as key in Message Resource bundle file
	   * @param {Array} params parameter that can used to set value in message
	   * @return {String} message string
	   */
	  getMessage : function(str, params) {
	    var msg;
	    try {
	      msg = eval("this." + str);
	    } catch (e) {
	    }
	    if (msg == null || msg == "undefined")
	      msg = str;
	
	    if (params != null && params.constructor.toString().indexOf("Array") > 0) {
	      for ( var i = 0; i < params.length; i++) {
	        msg = msg.replace("{" + i + "}", params[i]);
	      }
	    }
	    return msg;
	  },
	
	  SessionTimeout : "${SessionTimeout}",
	  TargetBlockNotFound : "${TargetBlockNotFound}",
	  BlockUpdateNotFound : "${BlockUpdateNotFound}",
	  DefaultTheme : "${DefaultTheme}",
	  PreviousMonth : "${PreviousMonth}",
	  NextMonth : "${NextMonth}",
	  PreviousYear : "${PreviousYear}",
	  NextYear : "${NextYear}",
	  NotFound : "${NotFound}",
	  InProgress : "${InProgress}",
	  weekdays : [
	      "${weekdays.sun}",
	      "${weekdays.mon}",
	      "${weekdays.tue}",
	      "${weekdays.wed}",
	      "${weekdays.thu}",
	      "${weekdays.fri}",
	      "${weekdays.sat}"
	  ],
	  monthnames  :[
          "${months.jan}",
          "${months.feb}",
          "${months.mar}",
          "${months.apr}",
          "${months.may}",
          "${months.jun}",
          "${months.jul}",
          "${months.aug}",
          "${months.sep}",
          "${months.oct}",
          "${months.nov}",
          "${months.dec}"
	  ],
	  Save : "${Save}",
    Cancel : "${Cancel}",
	  members : "${members}",
    managers : "${managers}",
    publishers : "${publishers}",
    redactors : "${redactors}"
	};
	
	return eXo.i18n.I18NMessage;
})();