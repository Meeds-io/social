/* eslint-disable new-cap */
/*
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

'use strict';
( function() {

  let balloonToolbar, 
    url,
    selectedText,
    selectionElem,
    isInputTextToolbar = false;

  CKEDITOR.plugins.add( 'linkBalloon', {
    requires: 'balloontoolbar',
    lang: ['en','fr'],
    icons: 'addLink,removeLink',
    init: function( editor ) { 
      editor.addCommand( 'addLink', new CKEDITOR.addLinkCommand() );
      editor.addCommand( 'removeLink', new CKEDITOR.removeLinkCommand() );
      if ( editor.ui.addButton ) {
        editor.ui.addButton( 'addLink', {
          label: editor.lang.link.toolbar,
          command: 'addLink',
          toolbar: 'addLink'
        } );
        editor.ui.addButton( 'removeLink', {
          label: editor.lang.link.unlink,
          command: 'removeLink',
          toolbar: 'removeLink'
        } );
      }
      editor.on('instanceReady', function( ) {
        initBalloonToolbar(editor);
        destroyInputTextPanel(editor);
        // Attach the balloon toolbar on selected text
        editor.document.on( 'mouseup', function() {
          setupMouseObserver(editor);
        });
        // Attach the balloon toolbar when the clicked text is a link
        editor.document.on( 'click', function() {
          setupClickObserver(editor, selectedText);
        });

        editor.on('contentDom', function () {
          const editable = editor.editable();
          editable.attachListener(editable, 'mouseup', function () {
            setupMouseObserver(editor);
          });
          editable.attachListener(editable, 'click', function () {
            setupClickObserver(editor, balloonToolbar, selectedText);
          });
        }); 
      });
    },
  });

  function setupMouseObserver(editor) {
    balloonToolbar.destroy();
    initBalloonToolbar(editor);
    const selection = editor.getSelection(),
      link = getSelectedLink(editor);
    if (!balloonToolbar.getItem('unlink')) {
      if (link) {
        addUnlinkItem(balloonToolbar);
      } 
    } else {
      if (!link) {
        balloonToolbar.deleteItem('unlink');
      } 
    }
    if ( selection && selection.getSelectedText().length > 0) {
      balloonToolbar.attach( selection );
    } else {
      balloonToolbar.hide();
    }
  }

  function setupClickObserver( editor, data) {
    const link = getSelectedLink(editor);
    if (isInputTextToolbar) {
      const linkText = link && getSelectedText(editor) || data;
      const linkElem = {
        url: url,
        linkText: linkText
      };
      if (data.length && url && url.length) {
        if (!link) {
          insertLinksIntoSelection(editor, linkElem);   
        } else {
          editLinkInSelection(editor, link, linkElem)
        } 
      }
      balloonToolbar.destroy();
      isInputTextToolbar = false;
    }
    if (link) {
      balloonToolbar.destroy();
      initBalloonToolbar(editor);
      addUnlinkItem(editor);
      balloonToolbar.attach( editor.getSelection() );
    }
  }

  function getSelectedLink(editor) {
    const ranges = editor.getSelection().getRanges();
    return editor.elementPath( ranges[0].getCommonAncestor() ).contains( 'a', 1 );
  }

  function addUnlinkItem(editor) {
    balloonToolbar.addItem('unlink', 
      new CKEDITOR.ui.button({
        command: 'removeLink',
        toolbar: 'removeLink',
        label: editor.lang.linkBalloon.unlink
      })
    );
  }

  function initInputTextToolbar(editor, data) {
    isInputTextToolbar = true;
    const link = getSelectedLink(editor);
    selectedText = getSelectedText(editor);
    selectionElem = editor.getSelection();
    balloonToolbar = new CKEDITOR.ui.balloonPanel( editor, {
      content: '<input type="text"'+
      'id="inputURL"' + 
      'class="mb-0"' +
      'style="margin-bottom:0px;border:none;padding-left:30px;"'+
      'name="inputURL"'+
      'placeholder="Paste or type a link"'+
      '>',
      width: 'auto',
      triangleWidth: 7,
      triangleHeight: 7
    });
    const urlInputValue = document.getElementById('inputURL');

    if (link) {
      urlInputValue.value = link.data( 'cke-saved-href' );
      url = urlInputValue.value;
    } 
    
    urlInputValue.onkeyup = function(event){
      url = urlInputValue.value;
      if (event.key === 'Enter') {
        const linkElem = {
          url: url,
          linkText: data
        };
        if (!link) {
          insertLinksIntoSelection(editor, linkElem);   
        } else {
          editLinkInSelection(editor, link, linkElem)
        } 
        balloonToolbar.destroy();
        isInputTextToolbar = true;
      }
    };
  }

  function destroyInputTextPanel(editor) {
    document.addEventListener('ballonPanelHidden', () => {
      balloonToolbar.destroy();
      isInputTextToolbar = false;
      initBalloonToolbar(editor);
      balloonToolbar.attach( editor.getSelection() );
    });
  }


  function initBalloonToolbar(editor) {
    balloonToolbar = new CKEDITOR.ui.balloonToolbar(editor);
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
        command: 'addLink',
        toolbar: 'test',
        label: editor.lang.linkBalloon.link
      }),
    });
  }

  // Insert new link to selected text

  function insertLinksIntoSelection( editor, data ) {
    const attributes = getLinkAttributes( editor, data);
    const range = selectionElem.getRanges()[0],
      style = new CKEDITOR.style( {
        element: 'a',
        attributes: attributes.set
      }),
      rangesToSelect = [];


    style.type = CKEDITOR.STYLE_INLINE;
    const text = new CKEDITOR.dom.text( data.linkText, editor.document );
    editor.editable().extractHtmlFromRange( range );
    range.insertNode( text );
    range.selectNodeContents( text );
    const nestedLinks = range._find( 'a' );

    for ( let j = 0; j < nestedLinks.length; j++ ) {
      nestedLinks[ j ].remove( true );
    }
    // Apply style.
    style.applyToRange( range, editor );

    rangesToSelect.push( range );
    selectionElem.selectRanges( rangesToSelect );
    selectedText = ''; 
  }

  // Edit existing link to selected text

  function editLinkInSelection( editor, selectedElement, data ) {
    const attributes = getLinkAttributes( editor, data ),
      ranges = [],
      range = editor.createRange(),
      href = selectedElement.data( 'cke-saved-href' );

    selectedElement.setAttributes( attributes.set );
    selectedElement.removeAttributes( attributes.removed );
    const isURLEqualDisplay = href === url;
    if (!isURLEqualDisplay) {
      range.setStartBefore( selectedElement );
      range.setEndAfter( selectedElement );
      ranges.push( range );
      selectionElem.selectRanges( ranges );
    }
  }

  function getLinkAttributes( editor, data ) {
    const set = {};
    set[ 'data-cke-saved-href' ] = ( data && CKEDITOR.tools.trim( data.url ) ) || '';
    set.target = '_blank';
    if ( set[ 'data-cke-saved-href' ] ) {
      set.href = set[ 'data-cke-saved-href' ];
    }
    const removed = {
      target: 1,
      onclick: 1,
      'data-cke-pa-onclick': 1,
      'data-cke-saved-name': 1,
      'download': 1
    };
    for ( const s in set ) {
      delete removed[ s ];
    }

    return {
      set: set,
      removed: CKEDITOR.tools.object.keys( removed )
    };
  }

  function getSelectedText(editor) {
    return editor.getSelection().getSelectedText();
  }

  // eslint-disable-next-line no-empty-function
  CKEDITOR.addLinkCommand = function() {};
  CKEDITOR.addLinkCommand.prototype = {
    exec: function( editor ) {
      balloonToolbar.destroy();
      initInputTextToolbar(editor, getSelectedText(editor));
      balloonToolbar.parts.title.remove();
      balloonToolbar.parts.panel.addClass( 'cke_balloontoolbar' );
      balloonToolbar.parts.panel.addClass( 'cke_inputTextBalloon' );
      balloonToolbar.attach( editor.getSelection() );
    }
  };

  // eslint-disable-next-line no-empty-function
  CKEDITOR.removeLinkCommand = function() {};
  CKEDITOR.removeLinkCommand.prototype = {
    exec: function( editor ) {
      const style = new CKEDITOR.style({ 
        element: 'a', 
        type: CKEDITOR.STYLE_INLINE, 
        alwaysRemoveElement: 1 
      });
      editor.removeStyle( style );
    }
  };
    
})();
