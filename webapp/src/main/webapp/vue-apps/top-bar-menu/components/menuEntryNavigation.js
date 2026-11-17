/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

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
