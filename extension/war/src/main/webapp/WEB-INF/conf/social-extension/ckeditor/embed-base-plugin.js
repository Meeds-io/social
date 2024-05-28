const oldEditorConfigFn = CKEDITOR.editorConfig;
CKEDITOR.editorConfig = function (config) {
  oldEditorConfigFn(config);
  CKEDITOR.plugins.addExternal('embedbaseOnlyVideo', '/commons-extension/eXoPlugins/embedbaseOnlyVideo/', 'plugin.js');
  CKEDITOR.plugins.addExternal('embedsemanticOnlyVideo', '/commons-extension/eXoPlugins/embedsemanticOnlyVideo/', 'plugin.js');
  document.addEventListener('rich-editor-ready', () => {
    var supportsOembed = config.supportsOembed;
    if (supportsOembed) {
      if (config.extraPlugins.indexOf('embedsemantic') >= 0) {
        config.removePlugins = `${config.extraPlugins},embedsemantic`;
      }
      if (config.extraPlugins.indexOf('embedbase') >= 0) {
        config.removePlugins = `${config.extraPlugins},embedbase`;
      }
      if (config.extraPlugins.indexOf('embedsemanticOnlyVideo') < 0) {
        config.extraPlugins = `${config.extraPlugins},embedsemanticOnlyVideo`;
      }
      if (config.extraPlugins.indexOf('embedbaseOnlyVideo') < 0) {
        config.extraPlugins = `${config.extraPlugins},embedbaseOnlyVideo`;
      }
    } else {
      config.removePlugins = `${config.removePlugins},embedsemantic,embedbase,embedsemanticOnlyVideo,embedbaseOnlyVideo`;
    }
  });
};
