/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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

export function getNavigationNodeUri(baseSiteUri, navigation) {
  const hasPage = !!navigation?.pageKey;
  const pageUrl = hasPage && `${baseSiteUri}${navigation.uri}`;
  navigation.nodeUri = getLinkNodeUri(navigation?.pageLink) || pageUrl;
  return navigation.nodeUri;
}

// the URL a link page leads to: a path of this site as stored, else the link
// Autolinker recognises in it, else the link as stored
function getLinkNodeUri(link) {
  return link && !link.startsWith?.('/') && Autolinker.parse(link, {
    urls: true,
    email: true,
    phone: true,
  })?.[0]?.getUrl?.() || link;
}

// the rule UserHomePageService applies to a home page: a path of this site or
// an http(s) URL, with no whitespace, control character, quote, `<`, `>`, `$`
// or backslash
// eslint-disable-next-line no-control-regex -- the control characters the server refuses
const HOME_URI_PATTERN = /^(\/(?![/\\])|https?:\/\/)[^ \t\n\x0B\f\r\x00-\x1F\x7F<>"'`$\\]*$/i;

const HOME_URI_MAX_LENGTH = 2048;

// a host name whose last label starts with a letter, an optional port, then
// a path, a query, a fragment or nothing; an address of digits only (an IP
// address, a dotted phone number alike) needs its scheme in the link
const HOST_PREFIX_PATTERN = /^([a-z0-9]([a-z0-9-]*[a-z0-9])?\.)*[a-z]([a-z0-9-]*[a-z0-9])?(:\d+)?([/?#]|$)/i;

// the URL a navigation node is saved, and recognised, as a user's home page,
// from the link a link page stores, else the node's path: a link without a
// scheme takes the page's own, and a link the home page can't be gives null
export function getNavigationNodeHomeUri(link) {
  const nodeUri = link?.trim?.();
  if (!nodeUri || /^(mailto|tel|sms|javascript|data|vbscript|file):/i.test(nodeUri)) {
    return null;
  }
  const hasScheme = /^([a-z][a-z0-9+.-]*:(?!\d)|\/)/i.test(nodeUri);
  if (!hasScheme && !HOST_PREFIX_PATTERN.test(nodeUri)) {
    // without a scheme, only a link that starts with a host name leads to a
    // page: not an email address nor a phone number
    return null;
  }
  const uri = hasScheme ? nodeUri : `${window.location.protocol}//${nodeUri}`;
  return uri.length <= HOME_URI_MAX_LENGTH && HOME_URI_PATTERN.test(uri) ? uri : null;
}

export function getNavigationNodeTarget(navigation) {
  navigation.nodeTarget = navigation?.target === 'SAME_TAB' && '_self' || '_blank';
  return navigation.nodeTarget;
}

export function getNavigationNodeRel(navigation) {
  navigation.nodeRel = navigation?.target === 'NEW_TAB' && 'nofollow noreferrer noopener' || null;
  return navigation.nodeRel;
}

/**
 * Whether the click on a menu entry replaces the page displayed. The menu then stays open, the focus on the entry,
 * until the new page is displayed: closed at once, it takes the focus away with it, to the start of a page that is
 * leaving. An entry that leaves the page in place (a new tab, a download, an anchor of this page, an email address or
 * a phone number) closes the menu and gives the focus back to what opens it.
 *
 * @param {Element} entry the menu entry's link
 * @param {MouseEvent} event the click on it
 * @returns {boolean} whether the page is replaced
 */
export function replacesPage(entry, event) {
  if (!entry?.getAttribute?.('href')) {
    return false;
  }
  const inNewTab = entry.target === '_blank' || event?.ctrlKey || event?.metaKey || event?.shiftKey || event?.altKey;
  const url = new URL(entry.href, window.location.href);
  const anchorOfThisPage = !!url.hash && url.origin === window.location.origin
    && url.pathname === window.location.pathname && url.search === window.location.search;
  return !inNewTab && !anchorOfThisPage && /^https?:$/.test(url.protocol);
}

/**
 * The entries the arrow keys move between in a menu's content: its links, and the entries that open a sub-menu.
 * A sub-menu's content is rendered apart from the menu that holds its entry, so its own entries are not among them.
 *
 * @param {Element} content the menu's content
 * @returns {Element[]} the entries, in their display order
 */
export function menuEntries(content) {
  return content ? [...content.querySelectorAll('a.v-list-item, .v-list-item[aria-haspopup]')] : [];
}

/**
 * The position of the entry that holds an element: the entry itself, or the chevron inside a page link that opens
 * its sub-menu.
 *
 * @param {Element[]} entries the menu's entries
 * @param {Element} element the element that has the focus
 * @returns {number} the entry's position, -1 when no entry holds the element
 */
export function entryIndexOf(entries, element) {
  return element ? entries.findIndex(entry => entry.contains(element)) : -1;
}

/**
 * Gives the keyboard focus to a menu entry, and makes it the entry Vuetify's menu activates on Enter.
 *
 * @param {Object} menu the v-menu component holding the entry
 * @param {Element} entry the entry
 * @returns {void}
 */
export function focusMenuEntry(menu, entry) {
  if (!entry) {
    return;
  }
  entry.focus();
  if (menu?.getTiles) {
    menu.getTiles();
    menu.listIndex = menu.tiles.indexOf(entry);
  }
}
