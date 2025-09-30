/*
 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 
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
( function() {
  let textWatcher;
  CKEDITOR.plugins.add( 'insertContentLink', {
    icons: 'insertContentLink',
    extraAllowedContent: 'a[data-*]',
    requires: 'textwatcher',
    init: function( editor ) {
      editor.addCommand( 'insertContentLink', {
        exec: function(editor) {
          window.require(['SHARED/ContentLink'], app => app.openDrawer(editor));
        }
      });

      editor.ui.addButton( 'InsertContentLink', {
        label: window.vueI18nMessages?.['richeditor.insertContent.tooltip'] || '',
        command: 'insertContentLink',
        toolbar: 'insert'
      });

      editor.on('getData', function(evt) {
        let textData = evt.data.dataValue;
  
        const element = document.createElement('div');
        element.innerHTML = textData;
  
        const contentLinks = element.querySelectorAll('a.content-link');
        contentLinks.forEach(linkElement => {
          textData = textData.replace(linkElement.outerHTML, `<content-link contenteditable="false" style="display: none;">/${linkElement.getAttribute('data-object')}</content-link>`);
        });
        evt.data.dataValue = textData;
      });
  
      editor.on('instanceReady', function(evt) {
        setData(evt);
      });
  
      editor.on('setData', function(evt) {
        setData(evt);
      });
  
      function setData(evt) {
        const editor = evt.editor;
        if (editor.setDataInProgress) {
          return;
        }
        let textData = evt?.data?.dataValue || editor?.document?.getBody?.()?.$?.innerHTML;
        if (!textData?.length) {
          return;
        }
  
        const element = document.createElement('div');
        element.innerHTML = textData;
  
        const contentLinks = element.querySelectorAll('content-link');
        if (contentLinks?.length) {
          Promise.all([...contentLinks.values()].map(linkElement => {
            const objectParts = linkElement?.innerText?.trim?.()?.replace?.('/', '')?.split?.(':');
            if (objectParts?.length === 2) {
              return fetch(`/social/rest/contentLinks/link/${objectParts[0]}/${objectParts[1]}`, {
                method: 'GET',
                credentials: 'include',
              }).then(resp => resp?.ok && resp.json()).then(link => {
                if (link) {
                  textData = textData.replace(linkElement.outerHTML, `<a href="${link.uri}" target="_blank" data-object="${link.objectType}:${link.objectId}" contenteditable="false" class="content-link"><i aria-hidden="true" class="v-icon notranslate ${link.icon} theme--light icon-default-color" style="font-size: 16px; margin: 0 4px;"></i>${link.title}</a>`);
                }
              });
            }
          })).finally(() => {
            editor.setDataInProgress = true;
            try {
              editor.setData(textData);
            } finally {
              delete editor.setDataInProgress;
            }
          });
        }
      }
  
  
      let menuOpen = false;
      let range;
      let text;
  
      function textTestCallback(range) {
        if (!range.collapsed) {
          return null;
        }
        return CKEDITOR.plugins.textMatch.match(range, matchCallback);
      }
  
      function matchCallback(text, offset) {
        const left = text.slice(0, offset);
        let match = left.match(/\/([a-z]*)(:[^>^<^.]*)?$/);
        if (match?.length) {
          return {
            start: match.index + 1,
            end: offset,
          };
        } else if (menuOpen) {
          textWatcher.unmatch();
          return null;
        }
      }
  
      function onTextMatched(evt) {
        editor.removeListener('blur', textWatcher.unmatch, textWatcher);
        if (!menuOpen) {
          getWindow().document.addEventListener('parent-element-scrolled', openCommandMenu);
          editor.removeListener('key', onKeyShortcut);
          editor.on('key', onKeyShortcut, null, null, 10);
          menuOpen = true;
        }
        range = evt.data.range;
        text = evt.data.text;
        openCommandMenu();
      }
  
      function onTextUnmatched() {
        editor.removeListener('key', onKeyShortcut);
        if (menuOpen) {
          menuOpen = false;
          getWindow().document.removeEventListener('parent-element-scrolled', openCommandMenu);
          closeCommandMenu();
        }
      }
  
      function openCommandMenu() {
        getWindow().require(['SHARED/ContentLink'], app => app.openCommandMenu({
          editor,
          textWatcher,
          command: text,
          range,
          position: getViewPosition(range),
        }));
      }
  
      function closeCommandMenu() {
        getWindow().require(['SHARED/ContentLink'], app => app.closeMenu(editor));
      }
  
      function onKeyShortcut(evt) {
        if (evt.data.keyCode == 27 // Escape
          || evt.data.keyCode == 40 // down
          || evt.data.keyCode == 38 // up
          || evt.data.keyCode == 13) { // enter
          evt.cancel();
          evt.stop();
          if (evt.data.keyCode == 40) {
            getWindow().document.dispatchEvent(new CustomEvent('custom-link-item-select-down'));
          } else if (evt.data.keyCode == 38) {
            getWindow().document.dispatchEvent(new CustomEvent('custom-link-item-select-up'));
          } else if (evt.data.keyCode == 13) {
            getWindow().document.dispatchEvent(new CustomEvent('custom-link-item-select'));
          } else if (evt.data.keyCode == 27) {
            getWindow().document.dispatchEvent(new CustomEvent('content-link-menu-close'));
          }
        }
      }
  
      function getWindow() {
        const editable = editor.editable();
        return editable.isInline() ? window : window.parent;
      }
  
      function getViewPosition(range) {
        const rects = range.getClientRects();
        const viewPositionRect = rects[ rects.length - 1 ];
        const editable = editor.editable();
        const offset = editable.isInline() ? CKEDITOR.document.getWindow().getScrollPosition() : editable.getParent().getDocumentPosition(CKEDITOR.document);
  
        let hostElement = CKEDITOR.document.getBody();
        if (hostElement.getComputedStyle('position') === 'static') {
          hostElement = hostElement.getParent();
        }
  
        const offsetCorrection = hostElement.getDocumentPosition();
        offset.x -= offsetCorrection.x;
        offset.y -= offsetCorrection.y;
  
        return {
          top: viewPositionRect.top + offset.y,
          bottom: viewPositionRect.top + viewPositionRect.height + offset.y,
          left: viewPositionRect.left + offset.x
        };
      }
  
      textWatcher = new CKEDITOR.plugins.textWatcher(editor, textTestCallback, 20);
      textWatcher.attach();
      textWatcher.on('matched', onTextMatched);
      textWatcher.on('unmatched', onTextUnmatched);
    },
  
    destroy: function() {
      onTextUnmatched();
      textWatcher.destroy();
      textWatcher = null;
    },
  });
})();