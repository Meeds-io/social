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
 * its beginning, with a note and the download link (EXO-94929). The renderer
 * runs on the page's thread and its time grows with the square of the text's
 * length: 64 KiB of tables or emphasis render in about 0.2 s, 256 KiB take
 * about 3 s, and 1 MiB can exhaust the tab's memory.
 */
export const MARKDOWN_PREVIEW_MAX_BYTES = 64 * 1024;

/**
 * The modules the preview renders with, loaded on demand when a Markdown
 * file is first previewed: the Markdown renderer, and the platform's filter
 * of the addresses outside the platform that EVA's answers use too.
 */
export const RENDERER_MODULES = ['SHARED/markdownShowdown', 'SHARED/externalResourceFilter'];

/**
 * The elements a previewed file never shows, whatever their attributes:
 * embedded frames and objects, forms, the platform's custom elements that
 * write markup of their own, and the document-level elements. The sanitizer
 * that runs afterwards keeps some of them for notes and posts (an iframe of
 * an allowed host, content-link), which a file must not carry.
 */
const REMOVED_ELEMENTS = 'iframe, frame, frameset, object, embed, applet, form, portal, content-link, user-by-name, base, meta, link';

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
 * Loads the Markdown renderer and the outside-address filter. A failed load
 * is forgotten by RequireJS, so that the next preview tries again rather
 * than waiting forever on the failed module RequireJS would otherwise keep.
 *
 * @returns {Promise<{showdown: object, filter: object}>} the showdown
 *          library, and the filter exposing withoutExternalResources
 */
export function loadRenderer() {
  return new Promise((resolve, reject) => {
    window.require(RENDERER_MODULES, (showdown, filter) => resolve({showdown, filter}), error => {
      RENDERER_MODULES.forEach(module => window.require.undef?.(module));
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
 * which sanitizes it last. Before that, the platform's filter removes every
 * address outside the platform the browser would load on display, as for
 * EVA's answers; then, on an inert document (DOMParser runs no script and
 * loads nothing), what that sanitizer keeps for notes and posts and a file
 * must not carry is removed: embedded frames and objects, forms, the
 * platform's custom elements and every is attribute. Every link opens in a
 * new tab.
 *
 * @param {string} markdown the file's text
 * @param {{showdown: object, filter: object}} renderer what loadRenderer resolves
 * @param {string} [origin] the platform's origin, the page's by default
 * @returns {string} the HTML to sanitize and display
 */
export function toPreviewHtml(markdown, renderer, origin) {
  if (!converter) {
    converter = new renderer.showdown.Converter(SHOWDOWN_OPTIONS);
  }
  const html = renderer.filter.withoutExternalResources(converter.makeHtml(markdown || ''), origin);
  const body = new DOMParser().parseFromString(html, 'text/html').body;
  body.querySelectorAll(REMOVED_ELEMENTS).forEach(element => element.remove());
  body.querySelectorAll('[is]').forEach(withoutIsValue);
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
