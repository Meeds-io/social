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
CKEDITOR.plugins.add( 'insertContent', {
  icons: 'insertContent',
  extraAllowedContent: 'a[data-*]',
  init: function( editor ) {
    editor.addCommand( 'insertContent', {
      exec: function( editor ) {
        window.require(["SHARED/ContentLink"], app => app.openDrawer(editor));
      }
    });

    editor.ui.addButton( 'InsertContent', {
      label: window.vueI18nMessages?.['richeditor.insertContent.tooltip'] || '',
      command: 'insertContent',
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

    editor.on('setData', function(evt) {
      const editor = evt.editor;
      if (editor.setDataInProgress) {
        return;
      }
      let textData = evt.data.dataValue;

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
    });
  }
});
