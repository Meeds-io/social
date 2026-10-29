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
export function getLinkNodeUri(link) {
  return link && !link.startsWith?.('/') && Autolinker.parse(link, {
    urls: true,
    email: true,
    phone: true,
  })?.[0]?.getUrl?.() || link;
}

// the rule UserHomePageService applies to a home page: a path of this site or
// an http(s) URL, with no whitespace, control character, quote, `<`, `>`, `$`
// or backslash
const HOME_URI_PATTERN = /^(\/(?![/\\])|https?:\/\/)[^ \t\n\x0B\f\r\x00-\x1F\x7F<>"'`$\\]*$/i;

const HOME_URI_MAX_LENGTH = 2048;

// a host name whose last label starts with a letter, an optional port, then
// a path, a query, a fragment or nothing; an address of digits only (an IP
// address, a dotted phone number alike) needs its scheme in the link
const HOST_PREFIX_PATTERN = /^([a-z0-9]([a-z0-9-]*[a-z0-9])?\.)*[a-z]([a-z0-9-]*[a-z0-9])?(:\d+)?([/?#]|$)/i;

// the URL a navigation node is saved, and recognised, as a user's home page,
// from the URL the node leads to (getNavigationNodeUri, getLinkNodeUri): a link
// still without a scheme takes the page's own, and a link the home page can't
// be gives null
export function getNavigationNodeHomeUri(nodeUri) {
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
