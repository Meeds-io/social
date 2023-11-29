/* eslint-disable new-cap */
CKEDITOR.plugins.add( 'linkBalloon', {
  requires: 'balloontoolbar,link',
  lang: ['en','fr'],
  init: function( editor ) {       
    editor.on('instanceReady', function( ) {
      const balloonToolbar = new CKEDITOR.ui.balloonToolbar(editor);
      balloonToolbar.addItems({
        link: new CKEDITOR.ui.button({
          command: 'link',
          toolbar: 'links,10',
          label: editor.lang.linkBalloon.link
        }),
        unlink: new CKEDITOR.ui.button({
          command: 'unlink',
          toolbar: 'links,20',
          label: editor.lang.linkBalloon.unlink
        }),
        bold: new CKEDITOR.ui.button({
          command: 'bold',
          toolbar: 'basicstyles,1',
          label: editor.lang.linkBalloon.bold
        }),
        italic: new CKEDITOR.ui.button({
          command: 'italic',
          toolbar: 'basicstyles,2',
          label: editor.lang.linkBalloon.italic
        }),
      });
      // Attach the balloon toolbar on selected text
      editor.document.on( 'mouseup', function() {
        const selectedElem = editor.getSelection();
        if ( selectedElem && selectedElem.getSelectedText().length > 0) {
          balloonToolbar.attach( selectedElem );
        } else {
          balloonToolbar.hide();
        }
      });
      // Attach the balloon toolbar when the clicked text is a link
      editor.document.on( 'click', function( evt ) {
        if (evt.data.$.target.nodeName.toLowerCase() === 'a') {
          balloonToolbar.attach( editor.getSelection() );
        }
      });    
    });
  } 
} );
