(function($, bannerUploader) {
  var UIUserNavigation = {
    MORE_LABEL: '',
    initNavigation: function(moreLabel) {
      UIUserNavigation.MORE_LABEL = moreLabel;

      const $popupStatus = $('.uiRelationshipAction .popStatusAnswer');
      const $parent = $popupStatus.parent();
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
        const _w = $(window).width();
        if ( _w  < 1025) {
          const tabContainer = $('ul.userNavigation');
          tabContainer.css('visibility', 'visible');
          return;
        }

        const ul = $('.userNavigation');
        const $container = $('#UIUserNavigationPortlet').closest('.UIRowContainer');
        let delta = 130;
        if ($container.hasClass('sticky')) {
          const $avt = $('.uiProfileMenu .userAvt');
          const $navHeader = $('.uiProfileMenu .profileMenuNav');
          delta = $avt.width() + $navHeader.width() + 20;
        }

        let index = calculateIndex(ul, delta);
        if (index < ul.find('li.item').length) {
          index = calculateIndex(ul, delta + 109);
        }
        UIUserNavigation.reInitNavigation(index);
      }

      function calculateIndex(ul, delta) {
        const maxWith = ul.innerWidth() - delta;
        const liElements = ul.find('li.item');

        let w = 0, index = 0;
        for (let i = 0; i < liElements.length; ++i) {
          const wElm = liElements.eq(i).width();
          if ((w + wElm) < maxWith) {
            w += wElm;
            index++;
          } else {
            break;
          }
        }
        return index;
      }

      function reset() {
        const ul = $('.userNavigation');
        const liElements = ul.find('li.item');

        const temp = $('<ul></ul>');
        temp.append(liElements);
        ul.empty().append(temp.find('li.item'));
      }

      $(document).ready(function(){
        const ul = $('.userNavigation');
        const liElements = ul.find('> li');
        liElements.addClass('item');
        autoMoveApps();
      });

      $(window).resize(function(){
        reset();
        autoMoveApps();
      });

      UIUserNavigation.initStickyBanner();
    },
  	reInitNavigation: function(index) {
  	  //
      const tabContainer = $('ul.userNavigation');
      const tabs = tabContainer.find('li.item');

      const dropDownMenu = $('<ul/>', {
        'class': 'dropdown-menu'
      });

      const dropDownToggle = $('<a/>', {
        'href': '',
        'class': 'dropdown-toggle',
        'data-toggle': 'dropdown'
      }).append($('<i/>', {
        'class': 'uiIconAppMoreButton'
      }))
        .append($('<span/>', {
          'text': UIUserNavigation.MORE_LABEL
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
  	      'class': 'dropdown pull-right'
  	    }).append(dropDownToggle).append(dropDownMenu);

        tabContainer.append(dropDown);
      }

      // swap position if needed
      const swappedEl = $(dropDown).find('li.active');
      if ( swappedEl.length > 0 ) {
        const targetEl = $(dropDown).prevAll('li:first');
        const copy_to = $(swappedEl).clone(true);
        const copy_from = $(targetEl).clone(true);
        $(swappedEl).replaceWith(copy_from);
        $(targetEl).replaceWith(copy_to);
      }

      $(tabContainer).css({'visibility': 'visible'});
      UIUserNavigation.initStickyBanner();
      let appScroll = null;
      const $menuApps = $('.uiProfileMenu .profileMenuApps');
      $menuApps.off('scroll').on('scroll', function() {
        appScroll = window.setTimeout(function() {
          $menuApps.scrollLeft(0);
        }, 10000);
      });
      const $tab = $('.uiProfileMenu .userNavigation');
      const $selectedTab = $tab.find('.active');
      if ($selectedTab && $selectedTab.length) {
        const left = $selectedTab.position().left;
        const screenWidth = $(window).width();

        if (left > (screenWidth / 2) && left < ($tab[0].scrollWidth - screenWidth / 2)) {
          console.log(`center left:${  left  } screenwidth/2: ${  screenWidth / 2}`);
          $tab.scrollLeft(left - screenWidth / 2);
        } else if (left > $tab.width() - screenWidth / 2) {
          console.log(`left:${  left  } screenwidth/2: ${  screenWidth / 2}`);
          $tab.scrollLeft(left);
        }
      }
  	},

  	initStickyBanner: function() {
  	  $(window).off('scroll.uiProfileMenu').on('scroll.uiProfileMenu', function() {
        const $container = $('#UIUserNavigationPortlet').closest('.UIRowContainer');
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

    initAvatar: function(uploaderId) {
      $(`${uploaderId  } .uiUploadFile`).on('click', function() {
        bannerUploader.selectFile(uploaderId);
      });
    },

    initBanner: function(uploaderId) {
      $('.bannerControls .uiUploadFile').on('click', function() {
        bannerUploader.selectFile(uploaderId);
      });
      $('.bannerControls [data-toggle="popover"]').popover();
    }
  };
  
  return UIUserNavigation;
})(jq, bannerUploader);
