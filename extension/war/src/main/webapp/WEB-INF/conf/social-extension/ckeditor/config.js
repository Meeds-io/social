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
  config.plugins = 'dialogui,dialog,about,a11yhelp,basicstyles,blockquote,panel,floatpanel,button,toolbar,enterkey,entities,popup,filebrowser,floatingspace,listblock,richcombo,format,horizontalrule,htmlwriter,wysiwygarea,indent,indentlist,fakeobjects,list,maximize,removeformat,showborders,sourcearea,specialchar,scayt,stylescombo,tab,table,notification,notificationaggregator,filetools,undo,panelbutton,colorbutton,autogrow,confighelper,uploadwidget,imageresize,autoembed,embedsemantic';

  CKEDITOR.plugins.addExternal('simpleLink', '/commons-extension/eXoPlugins/simpleLink/', 'plugin.js');
  CKEDITOR.plugins.addExternal('simpleImage', '/commons-extension/eXoPlugins/simpleImage/', 'plugin.js');
  CKEDITOR.plugins.addExternal('suggester', '/commons-extension/eXoPlugins/suggester/', 'plugin.js');
  CKEDITOR.plugins.addExternal('hideBottomToolbar', '/commons-extension/eXoPlugins/hideBottomToolbar/', 'plugin.js');
  CKEDITOR.plugins.addExternal('autoembed', '/commons-extension/eXoPlugins/autoembed/', 'plugin.js');
  CKEDITOR.plugins.addExternal('embedsemantic', '/commons-extension/eXoPlugins/embedsemantic/', 'plugin.js');
  CKEDITOR.plugins.addExternal('tagSuggester', '/commons-extension/eXoPlugins/tagSuggester/', 'plugin.js');
  CKEDITOR.plugins.addExternal('formatOption', '/commons-extension/eXoPlugins/formatOption/', 'plugin.js');
  CKEDITOR.plugins.addExternal('googleDocPastePlugin', '/commons-extension/eXoPlugins/googleDocPastePlugin/', 'plugin.js');
  CKEDITOR.plugins.addExternal('linkBalloon', '/social-portlet/js/ckeditorPlugins/linkBalloon/', 'plugin.js');

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
  document.querySelectorAll('[skin-type=portal-skin]')
    .forEach(link => config.contentsCss.push(link.href));
  config.contentsCss.push(document.querySelector('#brandingSkin').href);
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
  
  // Disable the version check by default
  config.versionCheck = false;

  // Here is configure for suggester
  var peopleSearchCached = {};
  var lastNoResultQuery = false;

  const retrievePeople = async function(url, query) {
    return !query?.length && Promise.resolve([]) || fetch(url, {credentials: 'include'})
      .then(resp => resp?.ok && resp.json())
  };

  let space = null;
  const membersLabel = eXo.i18n.I18NMessage.getMessage('members');
  const managersLabel = eXo.i18n.I18NMessage.getMessage('managers');
  const publishersLabel = eXo.i18n.I18NMessage.getMessage('publishers');
  const redactorsLabel = eXo.i18n.I18NMessage.getMessage('redactors');

  const getSpace = async function(spaceURL, spacePrettyName, spaceId) {
    if (!spacePrettyName && !spaceId && !spaceURL) {
      return Promise.resolve();
    }
    return space && Promise.resolve(space)
      || (spacePrettyName && fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/byPrettyName/${spacePrettyName}`, {credentials: 'include'}).then(resp => resp?.ok && resp.json()))
      || (spaceURL && fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/byGroupSuffix/${spaceURL}`, {credentials: 'include'}).then(resp => resp?.ok && resp.json()))
      || fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${spaceId}`, {credentials: 'include'}).then(resp => resp?.ok && resp.json());
  }

  const retrieveSpaceRoles = function(query, space) {
    const result = [];
    if (space) {
      if (space.membersCount && (!query?.length || membersLabel.toLowerCase().includes(query.toLowerCase()))) {
        result.push({
          uid: `member:${space.identityId}`,
          name: membersLabel,
          icon: 'fa-users',
        });
      }
      if (space.managersCount && (!query?.length || managersLabel.toLowerCase().includes(query.toLowerCase()))) {
        result.push({
          uid: `manager:${space.identityId}`,
          name: managersLabel,
          icon: 'fa-user-cog',
        });
      }
      if (space.redactorsCount && (!query?.length || redactorsLabel.toLowerCase().includes(query.toLowerCase()))) {
        result.push({
          uid: `redactor:${space.identityId}`,
          name: redactorsLabel,
          icon: 'fa-user-edit',
        });
      }
      if (space.publishersCount && (!query?.length || publishersLabel.toLowerCase().includes(query.toLowerCase()))) {
        result.push({
          uid: `publisher:${space.identityId}`,
          name: publishersLabel,
          icon: 'fa-paper-plane',
        });
      }
    }
    return result;
  };
  config.suggester = {
    suffix: '\u00A0',
    minLen: 0,
    renderMenuItem(item, parent) {
      parent.data('value', item.uid);
      if (item.icon) {
        return `<div style="display: inline-flex;width:26px;height:26px;"><i aria-hidden="true" class="v-icon fa ${item.icon}" style="font-size: 14px;margin:auto;"></i></div> ${item.name}`;
      } else {
        return `<div class="avatarSmall" style="display: inline-block;"><img src="${item.avatar}"></div> ${item.name}`;
      }
    },
    renderItem(item) {
      return `<span class="exo-mention"><i aria-hidden="true" class="v-icon fa ${item.icon}" style="font-size: 14px;"></i> ${item.name}<a href="#" class="remove"><i class="uiIconClose uiIconLightGray"></i></a></span>`;
    },
    sourceProviders: ['exo:people'],
    providers: {
      'exo:people': function(query, callback) {
        if (lastNoResultQuery && query.length > lastNoResultQuery.length) {
          if (query.substr(0, lastNoResultQuery.length) === lastNoResultQuery) {
            callback.call(this, []);
            return;
          }
        }
        var spaceURL = window.CKEDITOR.currentInstance.config.spaceURL;
        var spacePrettyName = window.CKEDITOR.currentInstance.config.spacePrettyName;
        var spaceId = window.CKEDITOR.currentInstance.config.spaceId;
        const key = `${query}#${spaceURL}#${spacePrettyName}#${spaceId}`;
        if (peopleSearchCached[key]) {
          callback.call(this, peopleSearchCached[key]);
        } else {
          peopleSearchCached[key] = [];
          getSpace(spaceURL, spacePrettyName, spaceId)
            .then(data => {
              space = data;
              var userName = eXo.env.portal.userName;
              var activityId = window.CKEDITOR.currentInstance.config.activityId;
              var typeOfRelation = window.CKEDITOR.currentInstance.config.typeOfRelation;
              var url = eXo.env.portal.context + '/' + eXo.env.portal.rest + '/social/people/suggest.json?nameToSearch=' + query + '&currentUser=' + userName + '&typeOfRelation=' + typeOfRelation;
              if (space) {
                url += '&spaceURL=' + space.prettyName;
              }
              if (window.CKEDITOR.currentInstance.config.activityId) {
                url += '&activityId=' + activityId;
              }
              peopleSearchCached[key] = retrieveSpaceRoles(query, space);
              return retrievePeople(url, query)
                .then(users => {
                  if (users?.length) {
                    users.forEach(user => {
                      peopleSearchCached[key].push({
                        uid: user.id.substr(1),
                        name: user.name,
                        avatar: user.avatar,
                      });
                    });
                  }
                  return peopleSearchCached[key];
                });
            })
            .finally(() => {
              if (peopleSearchCached[key].length == 0) {
                lastNoResultQuery = query;
              } else {
                lastNoResultQuery = false;
              }
              callback.call(this, peopleSearchCached[key]);
            })
        }
      }
    }
  };
};
