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
    isInputTextToolbar = false,
    balloonToolbarDisplayed = false;

  CKEDITOR.plugins.add( 'linkBalloon', {
    requires: 'balloontoolbar',
    lang: ['en','fr'],
    icons: 'addLink,removeLink',
    init: function( editor ) { 
      editor.addCommand( 'addLink', new CKEDITOR.addLinkCommand() );
      editor.addCommand( 'removeLink', new CKEDITOR.removeLinkCommand() );
      editor.on('instanceReady', function( ) {
        initBalloonToolbar(editor);
        destroyInputTextPanel(editor);
        // Attach the balloon toolbar on selected text
        editor.document.on( 'mouseup', function() {
          setupMouseObserver(editor);
        });
        // Attach the balloon toolbar when the clicked text is a link
        editor.document.on( 'click', function() {
          setupClickObserver(editor);
        });

        editor.on('contentDom', function () {
          const editable = editor.editable();
          editable.attachListener(editable, 'mouseup', function () {
            setupMouseObserver(editor);
          });
          editable.attachListener(editable, 'click', function () {
            setupClickObserver(editor);
          });
        }); 
        editor.on('key', function (e) {
          if (getSelectedText(editor).length >= 0 && balloonToolbarDisplayed) {
            balloonToolbar.destroy();
            balloonToolbarDisplayed = false;
          } else if (e.data.keyCode === 27) {
            document.dispatchEvent(new CustomEvent('close-editor-container'));
          }
        });
      });

      editor.setKeystroke( CKEDITOR.CTRL + 75 /*K*/, 'addLink' );
      document.addEventListener('keydown', (evt) => {
        const selection = editor.getSelection();
        evt = evt || window.event;
        if (evt.key === 'Escape' && isInputTextToolbar && selection && selection.getSelectedText().trim().length >= 0) {
          hideInputTextPanel(editor);
        }
      });
    },
  });

  function setupMouseObserver(editor) {
    const selection = editor.getSelection(),
        link = getSelectedLink(editor);
    if ( selection && selection.getSelectedText().trim().length > 0) {
      const balloonElement = document.querySelector(".cke_balloontoolbar .cke_balloon_content");
      const isVisible = balloonElement != null && balloonElement.innerHTML.trim() !== '';
      balloonToolbar.destroy();
      initBalloonToolbar(editor);

      if (!balloonToolbar.getItem('unlink')) {
        if (link) {
          addUnlinkItem(editor);
        }
      } else {
        if (!link) {
          balloonToolbar.deleteItem('unlink');
        }
      }
        const selectedElement = editor.getSelection().getRanges()[0].startContainer.$;
        if (!(selectedElement.parentElement.classList.contains("metadata-tag") || (selectedElement.classList && selectedElement.classList.contains("metadata-tag")))
            && !(selectedElement.parentElement.classList.contains("atwho-query") || (selectedElement.classList && selectedElement.classList.contains("atwho-query"))) && !isVisible) {
          balloonToolbar.attach( selection );
        }
    } else {
      balloonToolbar.hide();
    }
  }

  function setupClickObserver(editor) {
    const link = getSelectedLink(editor);
    if (link) {
      balloonToolbar.destroy();
      initBalloonToolbar(editor);
      addUnlinkItem(editor);
      balloonToolbar.attach( editor.getSelection() );
    }
  }

  function getSelectedLink(editor) {
    const ranges = editor.getSelection().getRanges(),
          selectedElement = editor.getSelection().getRanges()[0].startContainer.$;
    return editor.elementPath( ranges[0].getCommonAncestor() ).contains( 'a', 1 )
        && !(selectedElement.parentElement.classList.contains("metadata-tag") || (selectedElement.classList && selectedElement.classList.contains("metadata-tag")))
        && !(selectedElement.parentElement.classList.contains("atwho-query") || (selectedElement.classList && selectedElement.classList.contains("atwho-query"))) ;
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
    balloonToolbarDisplayed = false;
    const link = getSelectedLink(editor),
          linkHTMLElement = editor.elementPath( editor.getSelection().getRanges()[0].getCommonAncestor()).contains( 'a', 1 );
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
      urlInputValue.value = linkHTMLElement.data( 'cke-saved-href' );
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
          editLinkInSelection(editor, linkHTMLElement, linkElem);
        } 
        editor.fire('change');
        balloonToolbar.destroy();
        isInputTextToolbar = true;
        balloonToolbarDisplayed = false;
        
        editor.focus();
        // Obtain the current selection & range
        const currentRange = editor.getSelection().getRanges()[0];

        // Create a new range from the editor object
        const newRange = editor.createRange();

        // assign the newRange to move to the end of the current selection
        const moveToEnd = true;
        newRange.moveToElementEditablePosition(currentRange.endContainer, moveToEnd);

        // change selection
        const newRanges = [newRange];
        editor.getSelection().selectRanges(newRanges);
      }
    };
  }

  function destroyInputTextPanel(editor) {
    document.addEventListener('ballonPanelHidden', () => {
      hideInputTextPanel(editor);
    });
  }

  function hideInputTextPanel(editor) {
    balloonToolbar.destroy();
    isInputTextToolbar = false;
    initBalloonToolbar(editor);
    balloonToolbar.attach( editor.getSelection() );
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
        toolbar: 'addLink',
        label: editor.lang.linkBalloon.link
      }),
    });
    balloonToolbarDisplayed = true;
  }

  // Insert new link to selected text

  function insertLinksIntoSelection( editor, data ) {
    const attributes = getLinkAttributes(data);
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
    url = '';
  }

  // Edit existing link to selected text

  function editLinkInSelection( editor, selectedElement, data ) {
    const attributes = getLinkAttributes(data),
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
    selectedText = ''; 
    url = '';
  }

  function getLinkAttributes( data ) {
    const set = {},
      protocol = (data && data.url && data.url.indexOf('://') === -1 && !data.url.includes(eXo.env.portal.context)) ? 'https://' : '';
    set[ 'data-cke-saved-href' ] = protocol.length > 0 ? protocol + url : url;
    set.target = url.includes(eXo.env.portal.context) ? '_self' : '_blank';
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
    return editor.getSelection().getSelectedText().trim();
  }

  // eslint-disable-next-line no-empty-function
  CKEDITOR.addLinkCommand = function() {};
  CKEDITOR.addLinkCommand.prototype = {
    exec: function( editor ) {
      if (getSelectedLink(editor) || getSelectedText(editor).length > 0) {
        balloonToolbar.destroy();
        initInputTextToolbar(editor, getSelectedText(editor));
        balloonToolbar.parts.title.remove();
        balloonToolbar.parts.panel.addClass( 'cke_balloontoolbar' );
        balloonToolbar.parts.panel.addClass( 'cke_inputTextBalloon' );
        balloonToolbar.attach( editor.getSelection() );
        document.getElementById('inputURL').focus();
      }
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
