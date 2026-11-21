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

/**
 * The most bytes of a Markdown file the preview reads: a longer file shows
 * its beginning, with a note and the download link (EXO-94929).
 */
export const MARKDOWN_PREVIEW_MAX_BYTES = 1024 * 1024;

/**
 * The module that ships the Markdown renderer, loaded on demand when a
 * Markdown file is first previewed.
 */
export const SHOWDOWN_MODULE = 'SHARED/markdownShowdown';

/**
 * The elements a previewed file never shows, whatever their attributes:
 * embedded frames and objects, forms, the platform's custom elements that
 * write markup of their own, and the document-level elements. The sanitizer
 * that runs afterwards keeps some of them for notes and posts (an iframe of
 * an allowed host, content-link), which a file must not carry.
 */
const REMOVED_ELEMENTS = 'iframe, frame, frameset, object, embed, applet, form, portal, content-link, user-by-name, base, meta, link';

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
 * browser loads on display, as url(…).
 */
const CSS_VALUE_ATTRIBUTES = ['style', 'fill', 'stroke', 'filter', 'mask', 'clip-path', 'marker-start', 'marker-mid', 'marker-end', 'cursor'];

/**
 * The renderer's options: GitHub-style tables and task lists, no header ids
 * (an id could collide with the page's own), no autolinking of bare words,
 * no raw-HTML document wrapper.
 */
const SHOWDOWN_OPTIONS = {
  requireSpaceBeforeHeadingText: true,
  omitExtraWLInCodeBlocks: true,
  noHeaderId: true,
  ghCompatibleHeaderId: false,
  headerLevelStart: 1,
  simplifiedAutoLink: false,
  literalMidWordUnderscores: true,
  strikethrough: true,
  tables: true,
  tasklists: true,
  disableForced4SpacesIndentedSublists: true,
  ghMentions: false,
  emoji: false,
  underline: false,
  ellipsis: false,
  completeHTMLDocument: false,
  metadata: false,
};

let converter;

/**
 * Loads the Markdown renderer. A failed load is forgotten by RequireJS, so
 * that the next preview tries again rather than waiting forever on the
 * module it kept (social pitfall: RequireJS 2.0.1 keeps a failed module).
 *
 * @returns {Promise<object>} the showdown library
 */
export function loadShowdown() {
  return new Promise((resolve, reject) => {
    window.require([SHOWDOWN_MODULE], resolve, error => {
      window.require.undef?.(SHOWDOWN_MODULE);
      reject(error);
    });
  });
}

/**
 * Reads at most limit bytes of a file and decodes them as UTF-8, with the
 * viewer's own session. The download stops as soon as the limit is reached,
 * so a very large file is never fully transferred. A file cut short ends at
 * its last line break, so that no table row or code line is split.
 *
 * @param {string} url the file's download address
 * @param {number} [limit] the most bytes read, MARKDOWN_PREVIEW_MAX_BYTES by default
 * @returns {Promise<{text: string, truncated: boolean}>} the text read, and
 *          whether the file goes on beyond it
 */
export async function readBeginning(url, limit = MARKDOWN_PREVIEW_MAX_BYTES) {
  const response = await fetch(url, {credentials: 'include'});
  if (!response.ok) {
    throw new Error(`Error ${response.status} while reading the file`);
  }
  const decoder = new TextDecoder('utf-8');
  const state = {text: '', read: 0, truncated: false};
  if (response.body?.getReader) {
    await readChunks(response.body.getReader(), decoder, limit, state);
  } else {
    const bytes = new Uint8Array(await response.arrayBuffer());
    state.truncated = bytes.length > limit;
    state.text = decoder.decode(state.truncated ? bytes.subarray(0, limit) : bytes, {stream: true});
  }
  const truncated = state.truncated;
  let text = state.text + (truncated ? '' : decoder.decode());
  if (truncated) {
    const lastLineBreak = text.lastIndexOf('\n');
    text = lastLineBreak > 0 ? text.substring(0, lastLineBreak + 1) : text;
  }
  return {text, truncated};
}

/**
 * Reads a stream chunk after chunk, decoding each one, until it ends or the
 * limit is reached. Once the limit is reached, the stream is cancelled, so
 * that the rest of the file is not transferred.
 *
 * @param {ReadableStreamDefaultReader} reader the response body's reader
 * @param {TextDecoder} decoder the UTF-8 decoder, used in stream mode
 * @param {number} limit the most bytes read
 * @param {{text: string, read: number, truncated: boolean}} state the text
 *          decoded so far, the bytes read and whether the file was cut,
 *          updated in place
 * @returns {Promise<void>} resolved once the stream ended or was cut
 */
function readChunks(reader, decoder, limit, state) {
  return reader.read().then(({done, value}) => {
    if (done) {
      return;
    }
    const remaining = limit - state.read;
    if (value.length > remaining) {
      state.text += decoder.decode(value.subarray(0, remaining), {stream: true});
      state.truncated = true;
      reader.cancel().catch(() => {
        // the bytes needed are read; a failed cancel changes nothing shown
      });
      return;
    }
    state.text += decoder.decode(value, {stream: true});
    state.read += value.length;
    return readChunks(reader, decoder, limit, state);
  });
}

/**
 * Renders a Markdown file as the HTML its preview shows. The result is not
 * yet safe to insert: it is meant for the v-sanitized-document directive,
 * which sanitizes it last. Before that, on an inert document (DOMParser runs
 * no script and loads nothing), it removes what that sanitizer keeps for
 * notes and posts and a file must not carry: embedded frames and objects,
 * forms, the platform's custom elements and every is attribute, and every
 * address outside the platform the browser would load on display. Every
 * link opens in a new tab.
 *
 * @param {string} markdown the file's text
 * @param {object} showdown the showdown library
 * @param {string} [origin] the platform's origin, the page's by default
 * @returns {string} the HTML to sanitize and display
 */
export function toPreviewHtml(markdown, showdown, origin) {
  if (!converter) {
    converter = new showdown.Converter(SHOWDOWN_OPTIONS);
  }
  const html = converter.makeHtml(markdown || '');
  const platform = origin || window.location.origin;
  const body = new DOMParser().parseFromString(html, 'text/html').body;
  body.querySelectorAll(REMOVED_ELEMENTS).forEach(element => element.remove());
  body.querySelectorAll('[is]').forEach(withoutIsValue);
  removeExternalResources(body, platform);
  body.querySelectorAll('a[href]').forEach(link => {
    link.setAttribute('target', '_blank');
    link.setAttribute('rel', 'noopener noreferrer');
  });
  return body.innerHTML;
}

/**
 * Replaces an element written with an is attribute by a copy that names no
 * customized built-in element. Removing the attribute is not enough: the
 * parser records the element's is value, and the HTML serializer writes it
 * back as an is attribute when the element has none, so the copy is made by
 * createElement, which records none. The copy keeps every other attribute
 * and the element's children.
 *
 * @param {Element} element an element carrying an is attribute
 * @returns {void}
 */
function withoutIsValue(element) {
  const copy = element.ownerDocument.createElementNS(element.namespaceURI, element.tagName.toLowerCase());
  [...element.attributes]
    .filter(attribute => attribute.name.toLowerCase() !== 'is')
    .forEach(attribute => copy.setAttribute(attribute.name, attribute.value));
  copy.append(...element.childNodes);
  element.replaceWith(copy);
}

/**
 * Removes from a parsed content everything that would make the browser load
 * an address outside the platform as it shows it. An image outside the
 * platform becomes its alternative text, and one with none is dropped; an SVG
 * element loading an address outside the platform is dropped; every other
 * address loaded outside the platform (src, poster, background, data) is
 * removed from its element; a srcset is always removed; a style element, and
 * a style or SVG presentation attribute holding a parenthesis, which every
 * CSS function loading an address opens, are removed. A link stays a link:
 * it loads nothing until the reader clicks it. The rules are those of the AI
 * add-on's withoutExternalResources (EXO-93575), which is to use this one
 * (EXO-94932).
 *
 * @param {HTMLElement} body the parsed content, changed in place
 * @param {string} platform the platform's origin
 * @returns {void}
 */
function removeExternalResources(body, platform) {
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
}

/**
 * Removes from one element the addresses it would load outside the
 * platform: its srcset, a src, poster, background or data address outside
 * the platform, and a style or SVG presentation attribute holding a
 * parenthesis. An image keeps its src, which the caller reads to replace the
 * image by its alternative text.
 *
 * @param {Element} element an element of the content
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
 * Tells whether an address is served by the platform itself.
 *
 * @param {string} address an address, absolute or relative
 * @param {string} platform the platform's origin
 * @returns {boolean} true when it is an http(s) address of the platform
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
