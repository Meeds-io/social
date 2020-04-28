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
      var advancedSearchIcon = UISpaceSearch.profileSearch.find(".advancedSearch");
      $(advancedSearchIcon).click(function() {
        var searchEl = UISpaceSearch.profileSearch.find('#SpaceSearch');
        var searchElInput = UISpaceSearch.profileSearch.find('.selectize-input input[type="text"]');
        var value = searchElInput.val();
        if(!value) {
          value = $(searchEl)[0].selectize.getValue();
        }
        $(searchEl).val(value);
        $(searchEl).attr('value', value);
        UISpaceSearch.searchBtn.click();
        event.stopPropagation();
      });

      $(searchEl).keypress(function(event) {
        var e = event || window.event;
        var keynum = e.keyCode || e.which;  
        if(keynum == 13) {
          UISpaceSearch.searchBtn.click();     
          event.stopPropagation();
        }
      });

    $(searchEl).suggester({
      type : 'tag',
      plugins: ['restore_on_backspace'],
      preload: false,
      persist: true,
      addPrecedence: true,
      createOnBlur: true,
      highlight: false,
      hideSelected: true,
      openOnFocus: true,
      maxItems: 1,
      sourceProviders: ['exo:social'],
      valueField: 'text',
      labelField: 'text',
      searchField: ['text'],
      loadThrottle: null,
      onFocus: function() {
        $(searchEl)[0].selectize.positionDropdown();
        var value = $(searchEl)[0].selectize.getValue();
        if(value) {
          $(searchEl)[0].selectize.removeItem(value, false);
          $(searchEl)[0].selectize.setTextboxValue(value);
        }
      },
      onDropdownOpen: function($dropdown) {
      $(searchEl)[0].selectize.positionDropdown();
        var value = $(searchEl)[0].selectize.getValue();
        if(value) {
          $(searchEl)[0].selectize.removeItem(value, false);
          $(searchEl)[0].selectize.setTextboxValue(value);
        }
      },
      onItemAdd: function(value, $item) {
        if(this.selectedValue == value) {
          return;
        }
        $(searchEl)[0].selectize.setTextboxValue(value);
        $(searchEl).val(value);
        $(searchEl).attr('value', value);
        if($item.invalid === false) {
          UISpaceSearch.searchBtn.trigger("click");
          $(searchEl)[0].selectize.setTextboxValue(value);
        }
      },
      onItemRemove: function(value, $item) {
        this.selectedValue = value;
        $(searchEl)[0].selectize.setTextboxValue(value);
      },
      onInitialize: function() {
        var searchElInput = UISpaceSearch.profileSearch.find('.selectize-input input[type="text"]');
        searchElInput.keydown(function(event) {
          var e = event || window.event;
          var keynum = e.keyCode || e.which;
          if(keynum == 13) {
            UISpaceSearch.searchBtn.click();     
            event.stopPropagation();
          }
        });
        $(searchEl)[0].selectize.positionDropdown();
        var value = $(searchEl)[0].selectize.getValue();
        if(value) {
          $(searchEl)[0].selectize.removeItem(value, false);
          $(searchEl)[0].selectize.setTextboxValue(value);
        }
      },
      create: function(input) {
          return {'value': input, 'text': input, 'invalid': true};
      },
      renderItem: function(item, escape) {
         return '<span class="exo-mention">' + escape(item.text) + '</span>';
      },
      renderMenuItem: function(item, escape) {
        var avatar = item.avatarUrl;
        if (avatar == null) {
          avatar = '/eXoSkin/skin/images/system/SpaceAvtDefault.png';
        }
        if(!item.text) {
            item.text = item.value;
        }
        return '<div class="optionItem" data-value="' + item.text + '"><div class="avatarSmall optionAvatar"><img src="' + avatar + '"></div><div class="optionName">' + escape(item.text) + '</div></div>';
      },
      sortField: [{field: 'order'}, {field: '$score'}],
      providers: {
        'exo:social': function(query, callback) {
          if (query && query.length > 0) {
              $.ajax({
                  type: "GET",
                  url: buildURL(query),
                  complete: function(jqXHR) {
                      if(jqXHR.readyState === 4) {
                          var json = $.parseJSON(jqXHR.responseText)
                          if (json.options != null) {
                              callback(json.options);
                          }
                      }
                  }
              });
          }
        } 
      }
    });

      //
      function buildURL(query) {
        var restURL = "/" + eXo.social.portal.rest + eXo.social.portal.context + '/social/spaces/suggest.json?conditionToSearch=' + query;
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

