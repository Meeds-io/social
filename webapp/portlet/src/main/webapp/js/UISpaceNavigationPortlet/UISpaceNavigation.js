(function($) {
  const UISpaceNavigation = {
    lastSearchKeyword: '',
    completeHTMLForEmptyField: null,
    moreSpaceVisible: false,
    init: function(uicomponentId, mySpaceRestUrl, noSpace, selectSpaceAction) {
      const me = this;
      me.mySpaceRestUrl = mySpaceRestUrl;
      me.selectSpaceAction = selectSpaceAction;
      me.noSpace=noSpace;
      const textField = $(`#${  uicomponentId}`).find('input.searchText');
      textField.on('keyup', function() {
        me.onTextSearchChange(uicomponentId);
      });
        	me.completeHTMLForEmptyField = $(`#${  uicomponentId}`).find('ul.spaceNavigation').html();
        	me.moreSpaceVisible = $(`#${  uicomponentId}`).find('.moreSpace').is(':visible');
    },
    requestData: function(keyword, uicomponentId) {
      const me = this;

      $.ajax({
        async: true,
        url: `${me.mySpaceRestUrl  }?fields=id,url,displayName,avatarUrl&keyword=${  keyword}`,
        type: 'GET',
        data: '',
        success: function(data) {
          me.render(data, uicomponentId);
        }
      });
    },
    render: function(dataList, uicomponentId) {
      const me = this;
      me.dataList = dataList;

      const spacesListREsult = $(`#${  uicomponentId}`).find('ul.spaceNavigation');
      const spaces = dataList;
      let groupSpaces = '';
      for (i = 0; i < spaces.length; i++) {
        const spaceId = spaces[i].id;
        const spaceUrl = `${window.location.protocol  }//${  window.location.host  }/${  spaces[i].url}`;
        const name = spaces[i].displayName;
        let imageUrl=spaces[i].avatarUrl;
        if (imageUrl == null) {
          imageUrl = '/eXoSkin/skin/images/system/SpaceAvtDefault.png';
        }
        const spaceDiv = `${'<li class=\'spaceItem\'>'+'<a class=\'spaceIcon avatarMini\''
                    + '\' href=\''}${  spaceUrl  }' title='${  name  }'><img src='${imageUrl}'/>${
          name  }</a></li>`;
        groupSpaces += spaceDiv;
      }
      if (groupSpaces != ''){
        spacesListREsult.html(groupSpaces);
      } else {
        spacesListREsult.html(`<li class='noSpace'>${  me.noSpace  }</li>`) ;
      }
    },
    onTextSearchChange: function(uicomponentId) {
      const me = this;
      clearTimeout(me.timeout);
      me.timeout = setTimeout(function(){
        const textSearch = $(`#${  uicomponentId}`).find('input.searchText').val();
        if (textSearch != me.lastSearchKeyword) {
          me.lastSearchKeyword = textSearch;
	                if (!textSearch || !textSearch.trim().length) {
	                	$(`#${  uicomponentId}`).find('ul.spaceNavigation').html(me.completeHTMLForEmptyField);
	                	if (me.moreSpaceVisible) {
	                		$(`#${  uicomponentId}`).find('.moreSpace').show();
	                	} else {
	                		$(`#${  uicomponentId}`).find('.moreSpace').hide();
	                	}
	                } else {
	                    me.requestData(textSearch, uicomponentId);
	                    $(`#${  uicomponentId}`).find('.moreSpace').hide();
	                }
        }
      }, 300);
    },
    ajaxRedirect: function (url) {
      if (self == top) {
        window.location.href = url;
      } else {
        //Iframe case
        window.parent.location.href = url;
      }
    }
  };

  return UISpaceNavigation;
})($);
