// force env when using the eXo Android app (the eXo Android app uses a custom user agent which
// is not known by CKEditor and which makes it not initialize the editor)
var userAgent = navigator.userAgent.toLowerCase();
if (userAgent != null && userAgent.indexOf('exo/') == 0 && userAgent.indexOf('(android)') > 0) {
  CKEDITOR.env.mobile = true;
  CKEDITOR.env.chrome = true;
  CKEDITOR.env.gecko = false;
  CKEDITOR.env.webkit = true;
}

CKEDITOR.editorConfig = function(config) {

  // The configuration options below are needed when running CKEditor from source files.
  config.plugins = 'dialogui,dialog,about,a11yhelp,basicstyles,blockquote,panel,floatpanel,button,toolbar,enterkey,entities,popup,filebrowser,floatingspace,listblock,richcombo,format,horizontalrule,htmlwriter,wysiwygarea,indent,indentlist,fakeobjects,list,maximize,removeformat,showborders,sourcearea,specialchar,scayt,stylescombo,tab,table,notification,notificationaggregator,filetools,undo,wsc,panelbutton,colorbutton,autogrow,confighelper,uploadwidget,imageresize,confirmBeforeReload,autoembed,embedsemantic';

  CKEDITOR.plugins.addExternal('simpleLink', '/commons-extension/eXoPlugins/simpleLink/', 'plugin.js');
  CKEDITOR.plugins.addExternal('simpleImage', '/commons-extension/eXoPlugins/simpleImage/', 'plugin.js');
  CKEDITOR.plugins.addExternal('suggester', '/commons-extension/eXoPlugins/suggester/', 'plugin.js');
  CKEDITOR.plugins.addExternal('hideBottomToolbar', '/commons-extension/eXoPlugins/hideBottomToolbar/', 'plugin.js');
  CKEDITOR.plugins.addExternal('confirmBeforeReload', '/commons-extension/eXoPlugins/confirmBeforeReload/', 'plugin.js');
  CKEDITOR.plugins.addExternal('autoembed', '/commons-extension/eXoPlugins/autoembed/', 'plugin.js');
  CKEDITOR.plugins.addExternal('embedsemantic', '/commons-extension/eXoPlugins/embedsemantic/', 'plugin.js');
  CKEDITOR.plugins.addExternal('tagSuggester', '/commons-extension/eXoPlugins/tagSuggester/', 'plugin.js');
  CKEDITOR.plugins.addExternal('formatOption', '/commons-extension/eXoPlugins/formatOption/', 'plugin.js');
  CKEDITOR.plugins.addExternal('attachImage', '/commons-extension/eXoPlugins/attachImage/', 'plugin.js');
  CKEDITOR.plugins.addExternal('googleDocPastePlugin', '/commons-extension/eXoPlugins/googleDocPastePlugin/', 'plugin.js')

  const embedBaseApiEndpoint = '@JVMProp{io.meeds.iframely.url://ckeditor.iframe.ly/api/oembed?omit_script=1}';
  CKEDITOR.config.embed_provider = embedBaseApiEndpoint + (embedBaseApiEndpoint.includes('?') ? '&' : '?') + 'url={url}&callback={callback}';

  const iframelyApiKey = '@JVMProp{io.meeds.iframely.key:}';
  if (iframelyApiKey?.length && embedBaseApiEndpoint.includes('ckeditor.iframe.ly')) {
    CKEDITOR.config.embed_provider += '&api_key=' + iframelyApiKey;
  }

  config.extraPlugins = 'simpleLink,suggester,hideBottomToolbar';
  config.skin = 'moono-exo,/commons-extension/ckeditor/skins/moono-exo/';

  // Define changes to default configuration here.
  // For complete reference see:
  // http://docs.ckeditor.com/#!/api/CKEDITOR.config

  // The toolbar groups arrangement.
  config.toolbarGroups = [
    { name: 'basicstyles', groups: ['basicstyles', 'cleanup'] },
    { name: 'paragraph', groups: ['list', 'indent', 'blocks', 'align', 'bidi', 'paragraph'] }
  ];

  // Remove some buttons provided by the standard plugins, which are
  // not needed in the Standard(s) toolbar.
  config.removeButtons = 'Subscript,Superscript,Cut,Copy,Paste,PasteText,PasteFromWord,Undo,Redo,Scayt,Unlink,Anchor,Table,HorizontalRule,SpecialChar,Maximize,Source,Strike,Outdent,Indent,Format,BGColor,About';

  config.uploadUrl = eXo.env.server.context + '/upload?action=upload&uploadId=';

  // Enable the browser native spell checker
  config.disableNativeSpellChecker = false;

  // Set the most common block elements.
  config.format_tags = 'p;h1;h2;h3;pre';

  // Simplify the dialog windows.
  config.removeDialogTabs = 'image:advanced;link:advanced';

  // Move toolbar below the test area
  config.toolbarLocation = 'bottom';

  // Remove "More colors..." button
  config.colorButton_enableMore = false;

  // style inside the editor
  config.contentsCss = [];
  document.querySelectorAll('[skin-type=portal-skin]').forEach(link => config.contentsCss.push(link.href))
  config.contentsCss.push('/commons-extension/ckeditorCustom/contents.css'); // load last

  config.toolbar = [
    ['Bold', 'Italic', 'RemoveFormat',],
    ['-', 'NumberedList', 'BulletedList', 'Blockquote'],
    ['-', 'simpleLink'],
  ];

  config.height = 110;

  config.autoGrow_onStartup = true;
  config.autoGrow_minHeight = 110;

  config.language = eXo.env.portal.language || 'en';

  // image2 config of align classes
  config.image2_alignClasses = ['pull-left', 'text-center', 'pull-right'];

  // remove the white mask on dialog
  config.dialog_backgroundCoverColor = 'transparent';
  config.dialog_backgroundCoverOpacity = 1;

  // Here is configure for suggester
  var peopleSearchCached = {};
  var lastNoResultQuery = false;
  config.suggester = {
    suffix: '\u00A0',
    renderMenuItem: '<li data-value="${uid}"><div class="avatarSmall" style="display: inline-block;"><img src="${avatar}"></div>${name} (${uid})</li>',
    renderItem: '<span class="exo-mention">${name}<a href="#" class="remove"><i class="uiIconClose uiIconLightGray"></i></a></span>',
    sourceProviders: ['exo:people'],
    providers: {
      'exo:people': function(query, callback) {
        if (lastNoResultQuery && query.length > lastNoResultQuery.length) {
          if (query.substr(0, lastNoResultQuery.length) === lastNoResultQuery) {
            callback.call(this, []);
            return;
          }
        }
        if (peopleSearchCached[query]) {
          callback.call(this, peopleSearchCached[query]);
        } else {
          require(['SHARED/jquery'], function($) {
            var userName = eXo.env.portal.userName;
            var activityId = CKEDITOR.currentInstance.config.activityId;
            var typeOfRelation = CKEDITOR.currentInstance.config.typeOfRelation;
            var spaceURL = CKEDITOR.currentInstance.config.spaceURL;
            var url = window.location.protocol + '//' + window.location.host + eXo.env.portal.context + '/' + eXo.env.portal.rest + '/social/people/suggest.json?nameToSearch=' + query + '&currentUser=' + userName + '&typeOfRelation=' + typeOfRelation + '&spaceURL=' + spaceURL;
            if (CKEDITOR.currentInstance.config.activityId) {
              url += '&activityId=' + activityId;
            }
            $.getJSON(url, function(responseData) {
              var result = [];
              for (var i = 0; i < responseData.length; i++) {
                var d = responseData[i];
                var item = {
                  uid: d.id.substr(1),
                  name: d.name,
                  avatar: d.avatar
                };
                result.push(item);
              }

              peopleSearchCached[query] = result;
              if (peopleSearchCached[query].length == 0) {
                lastNoResultQuery = query;
              } else {
                lastNoResultQuery = false;
              }
              callback.call(this, peopleSearchCached[query]);
            });
          });
        }
      }
    }
  };
};
