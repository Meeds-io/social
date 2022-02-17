CKEDITOR.plugins.add( 'video', {
    icons: 'video',
    lang : ['en','fr'],
    init: function( editor ) {

        var pluginDirectory = this.path;
        editor.addContentsCss(pluginDirectory + 'video.css');

        editor.addCommand('video', new CKEDITOR.dialogCommand('videoDialog'));
        editor.ui.addButton( 'Video', {
            label: editor.lang.video.buttonTooltip,
            command: 'video',
            icon: CKEDITOR.plugins.getPath('video') + 'icons/icon_black.png'
        });

        CKEDITOR.dialog.add('videoDialog', this.path + 'dialogs/videoDialog.js');
    }
});