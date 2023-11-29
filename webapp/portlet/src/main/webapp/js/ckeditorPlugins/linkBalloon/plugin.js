/* eslint-disable new-cap */
CKEDITOR.plugins.add( 'linkBalloon', {
  requires: 'balloontoolbar,link',
  lang: ['en','fr'],
  init: function( editor ) {       
    editor.on('instanceReady', function( ) {
      const balloonToolbar = new CKEDITOR.ui.balloonToolbar(editor);
      balloonToolbar.addItems({
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
        link: new CKEDITOR.ui.button({
          command: 'link',
          toolbar: 'links,10',
          label: editor.lang.linkBalloon.link
        }),
      });
      // Attach the balloon toolbar on selected text
      editor.document.on( 'mouseup', function(evt) {
        const selectedElem = editor.getSelection();
        if (!balloonToolbar.getItem('unlink')) {
          if (isLink(evt)) {
            addUnlink(balloonToolbar);
          } 
        } else {
          if (!isLink(evt)) {
            balloonToolbar.deleteItem('unlink');
          } 
        }
        
        if ( selectedElem && selectedElem.getSelectedText().length > 0) {
          balloonToolbar.attach( selectedElem );
        } else {
          balloonToolbar.hide();
        }
      });
      // Attach the balloon toolbar when the clicked text is a link
      editor.document.on( 'click', function( evt ) {
        if (isLink(evt)) {
          addUnlink(balloonToolbar);
          balloonToolbar.attach( editor.getSelection() );
        }
      });    
    });
    function isLink(e) {
      return e.data.$.target.nodeName.toLowerCase() === 'a';
    }
    function addUnlink(toolbar) {
      toolbar.addItem('unlink', new CKEDITOR.ui.button({command: 'unlink',toolbar: 'links,20',label: editor.lang.linkBalloon.unlink}));
    }
  },
} );
