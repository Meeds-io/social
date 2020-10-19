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
(function($, bannerUploader) {
  var UIUserNavigation = {
    MORE_LABEL : "",
    initNavigation : function(moreLabel) {
      UIUserNavigation.MORE_LABEL = moreLabel;

      var $popupStatus = $('.uiRelationshipAction .popStatusAnswer');
      var $parent = $popupStatus.parent();
      $popupStatus.on('show.bs.modal', function() {
        $('body').append($popupStatus);
      }).on('hide.bs.modal', function() {
        $parent.append($popupStatus);
      });
      $popupStatus.find('div').click(function() {
        $(this).parent().modal('hide');
        return false;
      });

      //
      function autoMoveApps(){
        var _w = $(window).width();
        if ( _w  < 1025) {
          var tabContainer = $('ul.userNavigation');
          tabContainer.css('visibility', 'visible')
          return;
        }

        var ul = $('.userNavigation');
        var $container = $('#UIUserNavigationPortlet').closest('.UIRowContainer');
        var delta = 130;
        if ($container.hasClass('sticky')) {
          var $avt = $('.uiProfileMenu .userAvt');
          var $navHeader = $('.uiProfileMenu .profileMenuNav');
          delta = $avt.width() + $navHeader.width() + 20;
        }

        var index = calculateIndex(ul, delta);
        if (index < ul.find('li.item').length) {
          index = calculateIndex(ul, delta + 109);
        }
        UIUserNavigation.reInitNavigation(index);
      };

      function calculateIndex(ul, delta) {
        var maxWith = ul.innerWidth() - delta;
        var liElements = ul.find('li.item');

        var w = 0, index = 0;
        for (var i = 0; i < liElements.length; ++i) {
          var wElm = liElements.eq(i).width();
          if((w + wElm) < maxWith) {
            w += wElm;
            index++;
          } else {
            break;
          }
        }
        return index;
      }

      function reset() {
        var ul = $('.userNavigation');
        var liElements = ul.find('li.item');

        var temp = $('<ul></ul>');
        temp.append(liElements);
        ul.empty().append(temp.find('li.item'));
      };

      $(document).ready(function(){
        var ul = $('.userNavigation');
        var liElements = ul.find('> li');
        liElements.addClass('item');
        autoMoveApps();
      });

      $(window).resize(function(){
          reset();
          autoMoveApps();
      });

      UIUserNavigation.initStickyBanner();
    },
  	reInitNavigation : function(index) {
  	  //
      var tabContainer = $('ul.userNavigation');
      var tabs = tabContainer.find('li.item');

      var dropDownMenu = $('<ul/>', {
        'class' : 'dropdown-menu'
      });

      var dropDownToggle = $('<a/>', {
        'href' : '',
        'class' : 'dropdown-toggle',
        'data-toggle' : 'dropdown'
      }).append($('<i/>', {
                            'class' : 'uiIconAppMoreButton'
                          }))
        .append($('<span/>', {
                               'text' : UIUserNavigation.MORE_LABEL
                             })
        );

      // clear
      tabContainer.empty();

      // rebuild
      $.each(tabs, function(idx, el) {
        if (idx < index) {
          tabContainer.append(el);
        } else {
          dropDownMenu.append(el);
        }
      });

      if (dropDownMenu.children().length > 0) {
  	    var dropDown = $('<li/>', {
  	      'class' : 'dropdown pull-right'
  	    }).append(dropDownToggle).append(dropDownMenu);

        tabContainer.append(dropDown);
      };

      // swap position if needed
      var swappedEl = $(dropDown).find('li.active');
      if ( swappedEl.length > 0 ) {
        var targetEl = $(dropDown).prevAll('li:first');
        var copy_to = $(swappedEl).clone(true);
        var copy_from = $(targetEl).clone(true);
        $(swappedEl).replaceWith(copy_from);
        $(targetEl).replaceWith(copy_to);
      }

      $(tabContainer).css({"visibility":"visible"});
      UIUserNavigation.initStickyBanner();
     var appScroll = null;
     var $menuApps = $('.uiProfileMenu .profileMenuApps');
     $menuApps.off('scroll').on('scroll', function() {
       appScroll = window.setTimeout(function() {
         $menuApps.scrollLeft(0);
       }, 10000);
     });
     var $tab = $('.uiProfileMenu .userNavigation');
     var $selectedTab = $tab.find('.active');
     if($selectedTab && $selectedTab.length) {
       var left = $selectedTab.position().left;
       var screenWidth = $(window).width();

       if (left > (screenWidth / 2) && left < ($tab[0].scrollWidth - screenWidth / 2)) {
         console.log('center left:' + left + " screenwidth/2: " + (screenWidth / 2));
         $tab.scrollLeft(left - screenWidth / 2);
       } else if (left > $tab.width() - screenWidth / 2) {
         console.log('left:' + left + " screenwidth/2: " + (screenWidth / 2));
         $tab.scrollLeft(left);
       }
     }
  	},

  	initStickyBanner: function() {
  	  $(window).off('scroll.uiProfileMenu').on('scroll.uiProfileMenu', function() {
        var $container = $('#UIUserNavigationPortlet').closest('.UIRowContainer');
        if ($(window).scrollTop() > 130) {
         if (!$container.hasClass('sticky')) {
            $container.addClass('sticky');
            $(window).trigger('resize');
         }
        } else {
         if ($container.hasClass('sticky')) {
            $container.removeClass('sticky');
            $(window).trigger('resize');
         }
        }
      });
  	},

    initAvatar : function(uploaderId) {
      $(uploaderId + ' .uiUploadFile').on('click', function() {
          bannerUploader.selectFile(uploaderId);
      });
    },

    initBanner : function(uploaderId) {
      $('.bannerControls .uiUploadFile').on('click', function() {
          bannerUploader.selectFile(uploaderId);
      });
      $('.bannerControls [data-toggle="popover"]').popover();
    }
  };
  
  return UIUserNavigation;
})(jq, bannerUploader);
