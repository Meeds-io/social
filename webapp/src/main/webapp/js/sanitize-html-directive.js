/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
(function() {
  function renderSanitizedHtml(el, binding) {
    let content = binding.value;
    if (content) {
      content = content.replace(/]]&gt;/g, ']]>');
      content = content.replace(/&lt;!\[CDATA\[/g, '<![CDATA[');
      if (content && content.includes('<oembed>') && content.includes('</oembed>')) {
        content = content.replace(/<oembed>(.*)<\/oembed>/g, '');
      }
      if (content && content.includes('<div><![CDATA[') && content.includes(']]></div>')) {
        try {
          content = content.replace(/<div><!\[CDATA\[(.*)]]><\/div>/g, window.decodeURIComponent(content.match(/<div><!\[CDATA\[(.*)]]><\/div>/i)[1]));
        } catch(e) {
          content = content.replace(/<div><!\[CDATA\[(.*)]]><\/div>/g, content.match(/<div><!\[CDATA\[(.*)]]><\/div>/i)[1]);
        }
      }
    }
    if (!el.classList.contains('reset-style-box')) {
      el.classList.add('reset-style-box');
    }
    el.innerHTML = content && ExtendedDomPurify.purify(content) || '';
  }
  function renderSanitizedHtmlNoEmbed(el, binding) {
    let content = binding.value;
    if (content) {
      content = content.replace(/]]&gt;/g, ']]>');
      content = content.replace(/&lt;!\[CDATA\[/g, '<![CDATA[');
      if (content && content.includes('<oembed>') && content.includes('</oembed>')) {
        content = content.replace(/<oembed>(.*)<\/oembed>/g, '');
      }
      if (content && content.includes('<div><![CDATA[') && content.includes(']]></div>')) {
        content = content.replace(/<div><!\[CDATA\[(.*)]]><\/div>/g, '');
      }
      if (content && content.includes('<iframe') && content.includes('</iframe>')) {
        content = content.replace(/<div[^>]*><iframe[^>]*><\/iframe><\/div>/g, '');
      }

    }
    el.innerHTML = content && ExtendedDomPurify.purify(content) || '';
  }
  // A re-render of the host component must not rebuild an unchanged content:
  // rewriting innerHTML recreates every embedded iframe, which ends a playing
  // video and exits its fullscreen (EXO-90272). The class is re-added anyway,
  // since a dynamic :class patched before this hook may have dropped it.
  window.Vue.directive('sanitized-html', {
    bind: renderSanitizedHtml,
    update(el, binding) {
      if (binding.value !== binding.oldValue) {
        renderSanitizedHtml(el, binding);
      } else if (!el.classList.contains('reset-style-box')) {
        el.classList.add('reset-style-box');
      }
    },
  });
  window.Vue.directive('sanitized-html-no-embed', {
    bind: renderSanitizedHtmlNoEmbed,
    update(el, binding) {
      if (binding.value !== binding.oldValue) {
        renderSanitizedHtmlNoEmbed(el, binding);
      }
    },
  });
})();
