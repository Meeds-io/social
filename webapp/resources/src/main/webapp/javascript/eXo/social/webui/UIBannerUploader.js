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

(function($, uiMaskLayer) {
var UIBannerUploader = {
  init: function() {
    $('*[rel="tooltip"]').tooltip();
  },

  selectFile : function(uploaderId) {
    var $uploader = $(uploaderId);
    if(!$uploader.length || !$uploader.find('.uploadButton .btn').length) {
      console.log("No upload handler found");
      return;
    }

    $uploader.find('.uploadButton label.btn').first().click();

    var self = this;
    var mask = null;
    $uploader.find("input[type=file]").on("change", function() {
      if($(this).val()) {
        mask = self.showMask();
      }
    });

    $uploader.find('.progressBarFrame .percent').bind('DOMSubtreeModified', function() {
        var percent = $(this).html();
        if (percent == '100%') {
            $uploader.closest('form').find('.uiAction .btn').click();
            if(mask) {
              self.hideMask(mask);
              mask = null;
            }
        }
    });
  },
  showMask : function() {
    var mask = uiMaskLayer.createTransparentMask();
    $("#AjaxLoadingMask .uiIconClose").addClass("hidden");
    uiMaskLayer.showAjaxLoading();
    return mask;
  },
  hideMask : function(mask) {
    $("#AjaxLoadingMask .uiIconClose").removeClass("hidden");
    uiMaskLayer.removeMasks(mask);
    mask = null;
  }
};

return UIBannerUploader;
})($, uiMaskLayer);