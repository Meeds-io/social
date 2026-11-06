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
  
        const element = parseInert(textData);
  
        for (const linkElement of findOutsideNoscript(element, 'a.content-link')) {
          textData = textData.replace(linkElement.outerHTML, () => `<content-link contenteditable="false" style="display: none;">/${linkElement.getAttribute('data-object')}</content-link>`);
        }
        evt.data.dataValue = textData;
      });
  
      editor.on('instanceReady', function(evt) {
        setData(evt);
      });
  
      editor.on('setData', function(evt) {
        setData(evt);
      });
  
      // Parses the editor's data from the body of a standards-mode document of its own, as the page parses a fragment, where nothing it holds is loaded or run
      function parseInert(html) {
        return new DOMParser().parseFromString(`<!DOCTYPE html><body>${html}`, 'text/html').body;
      }

      // The page reads an HTML noscript's content as text, never elements: the links a conforming one holds are not picked
      function findOutsideNoscript(element, selector) {
        return [...element.querySelectorAll(selector)].filter(linkElement => !isInHtmlNoscript(linkElement));
      }

      function isInHtmlNoscript(element) {
        for (let ancestor = element.parentElement; ancestor; ancestor = ancestor.parentElement) {
          if (ancestor.localName === 'noscript' && ancestor.namespaceURI === 'http://www.w3.org/1999/xhtml') {
            return true;
          }
        }
        return false;
      }

      // Escapes a value written into a chip, as the content-link element does
      function escapeHtml(text) {
        return String(text || '')
          .replace(/&/g, '&amp;')
          .replace(/</g, '&lt;')
          .replace(/>/g, '&gt;')
          .replace(/"/g, '&quot;');
      }

      function setData(evt) {
        const editor = evt.editor;
        if (editor.setDataInProgress) {
          return;
        }
        let textData = evt?.data?.dataValue || editor?.document?.getBody?.()?.$?.innerHTML;
        if (!textData?.length) {
          return;
        }
  
        const element = parseInert(textData);
  
        const contentLinks = findOutsideNoscript(element, 'content-link');
        if (contentLinks.length) {
          Promise.all(contentLinks.map(linkElement => {
            const objectParts = linkElement?.innerText?.trim?.()?.replace?.('/', '')?.split?.(':');
            if (objectParts?.length === 2) {
              return fetch(`/social/rest/contentLinks/link/${objectParts[0]}/${objectParts[1]}`, {
                method: 'GET',
                credentials: 'include',
              }).then(resp => resp?.ok && resp.json()).then(link => {
                if (link) {
                  // A replacement function: the chip is inserted as is, with no $ pattern expanded
                  textData = textData.replace(linkElement.outerHTML, () => `<a href="${escapeHtml(link.uri)}" target="_blank" data-object="${link.objectType}:${link.objectId}" contenteditable="false" class="content-link"><i aria-hidden="true" class="v-icon notranslate ${link.icon} theme--light icon-default-color" style="font-size: 16px; margin: 0 4px;"></i>${escapeHtml(link.title)}</a>`);
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
          }).catch(e => console.warn('Error while rebuilding content links', e)); // eslint-disable-line no-console
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