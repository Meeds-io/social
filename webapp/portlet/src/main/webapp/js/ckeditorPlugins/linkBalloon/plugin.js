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
        setupMouseObserver(evt,balloonToolbar);
      });
      // Attach the balloon toolbar when the clicked text is a link
      editor.document.on( 'click', function( evt ) {
        setupClickObserver(evt, balloonToolbar);
      });

      editor.on('contentDom', function () {
        const editable = editor.editable();
        editable.attachListener(editable, 'mouseup', function (evt) {
          setupMouseObserver(evt,balloonToolbar);
        });
        editable.attachListener(editable, 'click', function (evt) {
          setupClickObserver(evt, balloonToolbar);
        });
      });  
    });
    function isLink(e) {
      return e.data.$.target.nodeName.toLowerCase() === 'a';
    }
    function addUnlink(toolbar) {
      toolbar.addItem('unlink', new CKEDITOR.ui.button({command: 'unlink',toolbar: 'links,20',label: editor.lang.linkBalloon.unlink}));
    }
    function setupMouseObserver(evt, toolbar) {
      const selectedElem = editor.getSelection();
      if (!toolbar.getItem('unlink')) {
        if (isLink(evt)) {
          addUnlink(toolbar);
        } 
      } else {
        if (!isLink(evt)) {
          toolbar.deleteItem('unlink');
        } 
      }
      if ( selectedElem && selectedElem.getSelectedText().length > 0) {
        toolbar.attach( selectedElem );
      } else {
        toolbar.hide();
      }
    }
    function setupClickObserver(evt, toolbar) {
      if (isLink(evt)) {
        addUnlink(toolbar);
        toolbar.attach( editor.getSelection() );
      }
    }
  },
} );
