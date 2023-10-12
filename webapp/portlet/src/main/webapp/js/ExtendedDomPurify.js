/**
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
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
  let ExtendedDomPurify = function() {
  };
  ExtendedDomPurify.prototype.purify = function(content) {
    content = content.replace(/<div> <\/div>/g, '<div><br><\/div>');
    content = content.trim().replace(/>[ \n]+</g, '> <').replace(/  /g, '&nbsp;&nbsp;');
    const pureHtml = DOMPurify.sanitize(Autolinker.link(content, {
      email: false,
      replaceFn : function (match) {
        switch(match.getType()) {
          case 'url' :
            if(match.getUrl().indexOf(window.location.origin) === 0) {
              return true;
            } else {
              const tag = match.buildTag();
              tag.setAttr('target', '_blank');
              tag.setAttr('rel', 'nofollow noopener noreferrer');
              return tag;
            }
          default :
            return true;
        }
      }
    }), {
      USE_PROFILES: {
        html: true,
        SAFE_FOR_TEMPLATES: true,
        svg: true
      },
      ADD_TAGS: ["iframe"],
      ADD_ATTR: ['target', 'allow', 'allowfullscreen', 'frameborder', 'scrolling', 'v-identity-popover'],
    });
    DOMPurify.addHook('afterSanitizeAttributes', function(node) {
      // add noopener attribute to external links to eliminate vulnerabilities
      if ('target' in node) {
        node.setAttribute('rel', 'noopener');
      }
    });
    DOMPurify.addHook('uponSanitizeElement', function(node) {
      if (node.tagName === 'iframe') {
        const src = node.getAttribute('src') || '';
        if (!src.startsWith('https://www.youtube.com/embed/')
          || !src.startsWith('https://player.vimeo.com/video/') || !src.startsWith('https://www.dailymotion.com/embed/video/')) {
          return node.parentNode?.removeChild(node);
        }
      }
    });
    return pureHtml;
  }
  return new ExtendedDomPurify();
})()
