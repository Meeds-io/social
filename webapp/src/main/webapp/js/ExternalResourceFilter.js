/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
// The platform's filter of the addresses outside the platform that rendered
// content would load on display (EXO-93575), shared by EVA's answers and the
// Markdown preview of the attachments (EXO-94929): module externalResourceFilter.
(function() {
  /**
   * The attributes whose address the browser loads as soon as the element is
   * shown, without a click: a link's href is not one of them.
   */
  const LOADED_ADDRESS_ATTRIBUTES = ['src', 'poster', 'background', 'data'];

  /**
   * The SVG elements that load the address of their href as soon as they are
   * drawn.
   */
  const SVG_LOADING_ELEMENTS = 'image, use, feImage';

  /**
   * The attributes whose value is a CSS value that may name an address the
   * browser loads on display, as url(…): style, and the SVG presentation
   * attributes taking a paint server, a clip, a mask, a filter, a marker or a
   * cursor.
   */
  const CSS_VALUE_ATTRIBUTES = ['style', 'fill', 'stroke', 'filter', 'mask', 'clip-path', 'marker-start', 'marker-mid', 'marker-end', 'cursor'];

  /**
   * A quick test for HTML holding at least one tag: text without any is
   * returned as it is. Any finer test misses what the HTML parser accepts, such
   * as a slash before an attribute's name.
   */
  const MAY_LOAD = /<[a-z]/i;

  /**
   * Removes from rendered content everything that would make the browser load an
   * address outside the platform as it shows it: a model may write an image of
   * a web service, or any other element that loads one, which an on-premise
   * platform must never load. An image outside the platform becomes its
   * alternative text, and one with none is dropped; an SVG element loading an
   * address outside the platform is dropped; every other address loaded
   * outside the platform (src, poster, background, data) is removed from its
   * element, which stays; a srcset is always removed; a style element, and a
   * style attribute or SVG presentation attribute (fill, stroke, filter, mask,
   * clip-path, marker-*, cursor) holding a parenthesis, which every CSS
   * function loading an address opens and no CSS escape can spell, are
   * removed. A link stays a link: it
   * loads nothing until the reader clicks it.
   *
   * @param {string} html the rendered HTML
   * @param {string} [origin] the platform's origin, the page's by default
   * @returns {string} the HTML, loading nothing outside the platform
   */
  function withoutExternalResources(html, origin) {
    if (!html || !MAY_LOAD.test(html)) {
      return html;
    }
    const platform = origin || window.location.origin;
    const body = new DOMParser().parseFromString(html, 'text/html').body;
    body.querySelectorAll('style').forEach(style => style.remove());
    body.querySelectorAll(SVG_LOADING_ELEMENTS).forEach(element => {
      const address = element.getAttribute('href') || element.getAttribute('xlink:href');
      if (address && !isPlatformAddress(address, platform)) {
        element.remove();
      }
    });
    body.querySelectorAll('*').forEach(element => keepPlatformAddresses(element, platform));
    body.querySelectorAll('img').forEach(image => {
      if (!isPlatformAddress(image.getAttribute('src'), platform)) {
        const alt = (image.getAttribute('alt') || '').trim();
        image.replaceWith(...(alt ? [body.ownerDocument.createTextNode(alt)] : []));
      }
    });
    return body.innerHTML;
  }

  /**
   * Removes from one element the addresses it would load outside the platform:
   * its srcset, a src, poster, background or data address outside the
   * platform, and a style or SVG presentation attribute holding a parenthesis
   * (CSS_VALUE_ATTRIBUTES). An image keeps its src,
   * which the caller reads to replace the image by its alternative text.
   *
   * @param {Element} element an element of the rendered content
   * @param {string} platform the platform's origin
   * @returns {void}
   */
  function keepPlatformAddresses(element, platform) {
    element.removeAttribute('srcset');
    const isImage = element.tagName.toLowerCase() === 'img';
    LOADED_ADDRESS_ATTRIBUTES.forEach(name => {
      const address = element.getAttribute(name);
      if (address !== null && !(isImage && name === 'src') && !isPlatformAddress(address, platform)) {
        element.removeAttribute(name);
      }
    });
    CSS_VALUE_ATTRIBUTES.forEach(name => {
      if ((element.getAttribute(name) || '').includes('(')) {
        element.removeAttribute(name);
      }
    });
  }

  /**
   * @param {string} address an image's address, absolute or relative
   * @param {string} platform the platform's origin
   * @returns {boolean} true when it is served by the platform itself
   */
  function isPlatformAddress(address, platform) {
    if (!address) {
      return false;
    }
    try {
      const url = new URL(address, platform);
      return (url.protocol === 'http:' || url.protocol === 'https:') && url.origin === platform;
    } catch (e) {
      return false;
    }
  }

  return {withoutExternalResources};
})();
