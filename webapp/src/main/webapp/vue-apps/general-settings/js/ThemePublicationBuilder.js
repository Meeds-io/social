/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import {copySettings, effectiveSettings, emptySettings, toThemePublication} from './ThemeSettingsMapper.js';

/**
 * The page-level working copy of the Branding and Theme page: everything the
 * administrator edits stays here until Apply publishes it at once.
 */

const IDENTITY_FILES = Object.freeze({
  logo: 'logo',
  darkLogo: 'darkLogo',
  favicon: 'favicon',
  darkFavicon: 'darkFavicon',
});

export const MODES = Object.freeze(['LIGHT_AND_DARK', 'LIGHT', 'DARK']);

const identityFile = fileId => ({fileId: fileId || null, uploadId: null, data: null, remove: false});

function workingTheme(theme) {
  return {
    key: String(theme.id),
    id: theme.id,
    clientId: null,
    name: theme.name,
    origin: theme.origin,
    configKey: theme.configKey,
    settings: copySettings(theme.settings),
    configSettings: theme.configSettings || null,
    pendingImages: {},
    dirty: false,
    restored: false,
  };
}

/**
 * @param {object} view the themes view the server returned
 * @returns {string} the company name in use, set on this page or configured
 */
function shownCompanyName(view) {
  return view.companyName || view.effectiveCompanyName || '';
}

/**
 * @param {object} view the themes view the server returned
 * @returns {object} the working copy of the page
 */
export function createWorkingCopy(view) {
  return {
    // the name in use, the configured one when none was set on this page
    companyName: shownCompanyName(view),
    identity: {
      logo: identityFile(view.logoFileId),
      darkLogo: identityFile(view.darkLogoFileId),
      favicon: identityFile(view.faviconFileId),
      darkFavicon: identityFile(view.darkFaviconFileId),
    },
    themes: (view.themes || []).map(workingTheme),
    deletedThemeIds: [],
    themeInUseKey: String(view.themeInUseId),
    modesOffered: view.modesOffered || MODES[0],
  };
}

let clientCounter = 0;

/**
 * @param {object} theme a theme a new one starts from
 * @returns {object} the theme as the server copies it: the Default Theme's configured dark values
 * (exo.branding.theme.dark.*) are its own defaults, not settings a copy takes
 */
function copyableSource(theme) {
  return theme?.origin === 'DEFAULT' ? Object.assign({}, theme, {configSettings: null}) : theme;
}

/**
 * @param {object} source the theme the new one starts from (an existing working theme)
 * @param {string} name the new theme's name
 * @param {Array} stored the themes as published, where the server's copy of the source comes from
 * @returns {object} a working theme that copies every setting of its source, light and dark versions included
 */
export function createWorkingTheme(source, name, stored) {
  clientCounter++;
  const base = effectiveSettings(copyableSource(source));
  // the server copies the published source: the difference starts from that, so the source's unsaved edits are part of it
  const published = source.id && stored?.find(item => item.id === source.id);
  const serverBase = published ? effectiveSettings(copyableSource(published)) : null;
  // an upload of the source is pending and its own; a source that is not published has no server-side files to copy
  const dropped = source.id ? /ImageUpload$/ : /Image(File|Upload)$/;
  [base].forEach(settings => Object.values(settings).forEach(map => Object.keys(map)
    .filter(key => dropped.test(key))
    .forEach(key => delete map[key])));
  const copy = copySettings(base);
  return {
    key: `new-${Date.now()}-${clientCounter}`,
    id: null,
    clientId: `new-${Date.now()}-${clientCounter}`,
    name,
    origin: 'UI',
    configKey: null,
    settings: copy,
    // what the server copies from a stored source, to send only the difference
    base: copySettings(serverBase || base),
    startFrom: source.id ? {id: source.id} : {clientId: source.clientId},
    imageSourceId: source.id || source.imageSourceId || null,
    configSettings: null,
    pendingImages: {},
    dirty: true,
    restored: false,
  };
}

/**
 * @param {object} theme a working theme
 * @returns {object} the theme back to the defaults its origin has: configured values for a configured theme,
 * the platform defaults otherwise
 */
export function createRestoredTheme(theme) {
  return Object.assign({}, theme, {
    settings: emptySettings(),
    pendingImages: {},
    dirty: true,
    restored: true,
  });
}

function filePublication(file) {
  if (file.uploadId) {
    return {uploadId: file.uploadId};
  }
  return file.remove && file.fileId ? {remove: true} : null;
}

/**
 * @param {object} working the page's working copy
 * @param {object} view the themes view the page loaded
 * @returns {object} the publication holding every change made since the page loaded `view`
 */
export function buildPublication(working, view) {
  const keptKeys = new Set(working.themes.map(theme => theme.key));
  const publication = {
    version: view.version,
    branding: {},
    themes: working.themes.filter(theme => theme.dirty).map(toThemePublication),
    deletedThemeIds: working.deletedThemeIds.filter(id => !keptKeys.has(String(id))),
  };
  // sent only when changed: an untouched configured name stays the configured default
  if (working.companyName !== shownCompanyName(view)) {
    publication.branding.companyName = working.companyName || '';
  }
  Object.keys(IDENTITY_FILES).forEach(key => publication.branding[key] = filePublication(working.identity[key]));
  if (working.themeInUseKey !== String(view.themeInUseId)) {
    const inUse = working.themes.find(theme => theme.key === working.themeInUseKey);
    publication.themeInUse = inUse?.id ? {id: inUse.id} : {clientId: inUse?.clientId};
  }
  if (working.modesOffered !== view.modesOffered) {
    publication.modesOffered = working.modesOffered;
  }
  return publication;
}

/**
 * @param {object} publication a publication built by buildPublication
 * @returns {boolean} true when the publication changes something: a page that only changed its custom CSS publishes nothing
 */
export function publicationHasChanges(publication) {
  const branding = publication.branding || {};
  return publication.themes.length > 0
    || publication.deletedThemeIds.length > 0
    || !!publication.themeInUse
    || !!publication.modesOffered
    || Object.prototype.hasOwnProperty.call(branding, 'companyName')
    || Object.keys(IDENTITY_FILES).some(key => !!branding[key]);
}
