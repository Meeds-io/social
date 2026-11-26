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

// The page's display mode, written by the server on <html data-display-mode="auto|light|dark">: the branding
// stylesheet renders the version it names, auto following the device. displayModeChanged (detail: the version the page
// now shows, light or dark) is the one signal a mode change sends: the components that read a colour in JavaScript,
// the editor frames and the page editor listen to it. Stateless: the state is the attribute.

const DARK_DEVICE = '(prefers-color-scheme: dark)';

const ATTRIBUTE_BY_MODE = {
  AUTOMATIC: 'auto',
  LIGHT: 'light',
  DARK: 'dark',
};

/**
 * @param {Document} [doc] the document, the page's by default
 * @returns {string} the attribute value: auto, light or dark
 */
export function getPageMode(doc) {
  return (doc || document).documentElement.getAttribute('data-display-mode') || 'light';
}

/**
 * @param {Document} [doc] the document, the page's by default
 * @returns {string} the version the document shows: light or dark
 */
export function getVersion(doc) {
  const mode = getPageMode(doc);
  if (mode === 'auto') {
    return window.matchMedia?.(DARK_DEVICE).matches ? 'dark' : 'light';
  }
  return mode;
}

/**
 * @param {string} mode AUTOMATIC, LIGHT or DARK
 * @returns {string} the attribute value of that mode
 */
export function toAttribute(mode) {
  return ATTRIBUTE_BY_MODE[mode] || 'auto';
}

/**
 * Renders the page in a mode, without reload, and tells the components that follow it.
 *
 * @param {string} mode AUTOMATIC, LIGHT or DARK
 * @returns {void}
 */
export function applyMode(mode) {
  const attribute = toAttribute(mode);
  document.documentElement.setAttribute('data-display-mode', attribute);
  applyFavicon(attribute);
  dispatchChange(document);
}

// The favicon links the page wrote (portal UIPortalApplication.gtmpl) carry both paths: the main one shows the
// version the page shows, the device's in Automatic, which the server cannot know when it writes it; the dark one,
// for a browser without JavaScript, is chosen by the device in Automatic only
function applyFavicon(attribute) {
  const favicon = document.getElementById('brandingFavicon');
  if (favicon?.dataset?.lightHref) {
    favicon.href = getVersion(document) === 'dark' ? favicon.dataset.darkHref : favicon.dataset.lightHref;
  }
  const darkFavicon = document.getElementById('brandingDarkFavicon');
  if (darkFavicon) {
    darkFavicon.media = attribute === 'auto' ? DARK_DEVICE : 'not all';
  }
}

/**
 * @param {Document} [doc] the document whose version changed, the page's by default
 * @returns {void}
 */
export function dispatchChange(doc) {
  (doc || document).dispatchEvent(new CustomEvent('displayModeChanged', {detail: getVersion(doc)}));
}

/**
 * While the page is in Automatic, a change of the device's mode changes the version shown: it is signalled like a
 * click, and the favicon follows. Registered once per page, when the favicon takes the version the page shows.
 *
 * @returns {void}
 */
export function listenToDevice() {
  if (window.displayModeDeviceListener || !window.matchMedia) {
    return;
  }
  applyFavicon(getPageMode(document));
  window.displayModeDeviceListener = () => {
    if (getPageMode(document) === 'auto') {
      applyFavicon('auto');
      dispatchChange(document);
    }
  };
  window.matchMedia(DARK_DEVICE).addEventListener('change', window.displayModeDeviceListener);
}
