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

/**
 * Translations between a stored theme (the shared, light and dark maps of the
 * themes REST API) and the flat style the area drawers edit.
 */

export const CONFIG_NAME_KEY = 'name';
export const FILE_SUFFIX = 'File';
export const UPLOAD_SUFFIX = 'Upload';

// image key of the stored maps -> file field the area drawers edit
export const IMAGE_FILE_FIELDS = Object.freeze({
  topBarBackgroundImage: 'topBarBackground',
  sideBarBackgroundImage: 'sideBarBackground',
  drawerBackgroundImage: 'drawerBackground',
  appBackgroundImage: 'appBackground',
  appTextTitleBackgroundImage: 'appTextTitleBackground',
  appTextHeaderBackgroundImage: 'appTextHeaderBackground',
  pageBackgroundImage: 'pageBackground',
});

// the keys the area drawers own: any other stored key is carried through untouched
// the colors a drawer shows with their calculated value (the toggles of border and background read their own keys)
const DISPLAYED_COLORS = Object.freeze([
  'primaryColor', 'secondaryColor', 'pageBackgroundColor',
  'topBarBackgroundColor', 'topBarTextColor', 'topBarIconColor',
  'sideBarBackgroundColor', 'sideBarTextColor', 'sideBarTextSubtitleColor', 'sideBarIconColor',
  'drawerBackgroundColor', 'drawerTextTitleColor', 'drawerTextHeaderColor', 'drawerTextColor', 'drawerTextSubtitleColor',
  'drawerIconColor', 'appTextTitleColor', 'appTextHeaderColor', 'appTextColor', 'appTextSubtitleColor', 'appIconColor',
]);

const TEXT_COLOR = /Text(Title|Header|Subtitle)?Color$/;

const MANAGED_KEY = /^(topBar|sideBar|drawer|app|page|primaryColor$|secondaryColor$|fontFamily$|borderRadius$)/;

/**
 * Mirrors the server's version placement (portal ThemeVariables#isShared):
 * the font, text sizes, bold and italic and the layout are shared by both
 * versions, colors, images and gradients are per version.
 *
 * @param {string} key a theme variable
 * @returns {boolean} true when both versions share the variable
 */
export function isSharedKey(key) {
  return key === 'fontFamily'
    || key === 'topBarSticky'
    || key === 'pageWidth'
    || key === 'borderRadius'
    || /(FontSize|FontStyle|FontWeight|BorderSize)$/.test(key)
    || key.includes('Margin')
    || key.includes('Radius')
    || key.includes('BackgroundPadding');
}

const isFileKey = key => key.endsWith(`Image${FILE_SUFFIX}`);
const isManaged = key => MANAGED_KEY.test(key) && !isFileKey(key) && !key.endsWith(UPLOAD_SUFFIX);

export function emptySettings() {
  return {shared: {}, light: {}, dark: {}};
}

export function copySettings(settings) {
  return {
    shared: Object.assign({}, settings?.shared),
    light: Object.assign({}, settings?.light),
    dark: Object.assign({}, settings?.dark),
  };
}

/**
 * The look a theme has: its configured values (a theme bound to the
 * configuration) under its stored ones, as the server builds a copy's base.
 *
 * @param {object} theme a theme with its settings and configSettings
 * @returns {object} the effective shared, light and dark maps
 */
export function effectiveSettings(theme) {
  const result = copySettings(theme.configSettings);
  delete result.shared[CONFIG_NAME_KEY];
  ['shared', 'light', 'dark'].forEach(version => Object.assign(result[version], theme.settings?.[version]));
  return result;
}

/**
 * Mirrors the server's LegacyBrandingImporter#normalizeColor: lower case, #rgb expanded, an opaque alpha dropped.
 *
 * @param {string} value a value
 * @returns {string} the normalized color, null when the value is no hex color
 */
export function normalizeColor(value) {
  let color = String(value).trim().toLowerCase();
  if (!/^#([0-9a-f]{3,4}|[0-9a-f]{6}|[0-9a-f]{8})$/.test(color)) {
    return null;
  }
  if (color.length <= 5) {
    color = `#${color.substring(1).split('').map(char => char + char).join('')}`;
  }
  return color.length === 9 && color.endsWith('ff') ? color.substring(0, 7) : color;
}

function sameValue(a, b) {
  const colorA = normalizeColor(a);
  const colorB = normalizeColor(b);
  if (colorA && colorB) {
    return colorA === colorB;
  }
  return String(a).trim().toLowerCase() === String(b).trim().toLowerCase();
}

const stable = map => JSON.stringify(Object.keys(map || {}).sort((a, b) => a.localeCompare(b)).map(key => [key, map[key]]));

/**
 * @param {object} a settings
 * @param {object} b settings
 * @returns {boolean} true when both hold the same values, whatever the order of their keys
 */
export function sameSettings(a, b) {
  return ['shared', 'light', 'dark'].every(version => stable(a?.[version]) === stable(b?.[version]));
}

const NEUTRAL_VALUES = Object.freeze(['unset', 'no-repeat']);

function isUnset(value) {
  return value == null || value === '' || value === 'initial';
}

/**
 * @param {object} theme the working theme
 * @param {object} defaults the platform defaults (GET /v1/platform/branding/default)
 * @param {string} version 'light' or 'dark', the version the area drawers edit
 * @param {object} resolved the server's resolved values of that version, null until known
 * @returns {object} a branding-shaped model for the area drawers: the effective theme style of the version,
 * the page values and the background files
 */
export function toEditorBranding(theme, defaults, version, resolved) {
  const effective = effectiveSettings(theme);
  const dark = version === 'dark';
  // the light values are what an Automatic dark key falls back to: they keep the toggles (border, title background)
  // and their shared companions in the dark model, and the dark untouched test keeps them out of the dark map
  const themeStyle = Object.assign({}, defaults, effective.shared, effective.light);
  if (dark) {
    Object.assign(themeStyle, effective.dark);
    // an Automatic dark color shows the value the server calculates; nothing else of the resolved values is a baseline
    DISPLAYED_COLORS.filter(key => !(key in effective.dark) && resolved?.[key]).forEach(key => themeStyle[key] = resolved[key]);
  } else {
    // an Auto text color of the light version too: over a background the administrator chose it shows its calculated value
    DISPLAYED_COLORS.filter(key => TEXT_COLOR.test(key) && !(key in effective.light) && resolved?.[key])
      .forEach(key => themeStyle[key] = resolved[key]);
  }
  const branding = {themeStyle};
  const restPath = `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/platform/branding`;
  const themeId = theme.id || theme.imageSourceId;
  Object.keys(IMAGE_FILE_FIELDS).forEach(imageKey => {
    const own = effective[version];
    // a dark version without its own image shares the light one
    const fileId = Number(own[imageKey + FILE_SUFFIX] || effective.light[imageKey + FILE_SUFFIX]) || 0;
    const uploadId = own[imageKey + UPLOAD_SUFFIX];
    const pending = theme.pendingImages?.[`${version}:${imageKey}`];
    branding[IMAGE_FILE_FIELDS[imageKey]] = {
      fileId: uploadId ? 0 : fileId,
      updatedDate: fileId,
      uploadId: uploadId || null,
      data: uploadId && pending?.data || null,
      imageUrl: !uploadId && fileId && themeId
        && `${restPath}/themes/${themeId}/images/${imageKey}?version=${version}&v=${fileId}` || null,
    };
    // a drawer shows the gradient only: the image has its own file
    delete themeStyle[imageKey + FILE_SUFFIX];
    delete themeStyle[imageKey + UPLOAD_SUFFIX];
  });
  branding.pageWidth = themeStyle.pageWidth;
  branding.pageBackgroundColor = themeStyle.pageBackgroundColor;
  branding.pageBackgroundSize = themeStyle.pageBackgroundSize;
  branding.pageBackgroundRepeat = themeStyle.pageBackgroundRepeat;
  branding.pageBackgroundPosition = themeStyle.pageBackgroundPosition;
  branding.pageBackgroundAttachment = themeStyle.pageBackgroundAttachment;
  branding.pageBackgroundEffect = themeStyle.pageBackgroundImage;
  return branding;
}

/**
 * Writes what the area drawers edited into a copy of the theme's settings.
 * Shared values go to the shared map whatever the version; the others to
 * the map of the edited version. A version value the administrator did not
 * change and the theme did not store stays absent: it is Automatic (or
 * Auto), calculated by the server.
 *
 * @param {object} theme the working theme
 * @param {object} edited the area drawers' result: {themeStyle, files: {<file field>: file}}
 * @param {object} defaults the platform defaults
 * @param {string} version 'light' or 'dark'
 * @param {object} baseline the style the drawers started from, to tell a changed value from an untouched one
 * @returns {{settings: object, pendingImages: object}} the new settings of the theme
 */
export function fromEditorResult(theme, edited, defaults, version, baseline) {
  const settings = copySettings(theme.settings);
  const pendingImages = Object.assign({}, theme.pendingImages);
  const configured = effectiveSettings({configSettings: theme.configSettings});
  const defaultOf = key => configured.shared[key] ?? configured[version][key] ?? defaults?.[key];
  const previous = {shared: theme.settings?.shared || {}, [version]: theme.settings?.[version] || {}};
  ['shared', version].forEach(map => {
    Object.keys(settings[map]).filter(isManaged).forEach(key => delete settings[map][key]);
  });
  Object.keys(edited.themeStyle || {}).forEach(key => {
    const value = edited.themeStyle[key];
    const map = isSharedKey(key) ? 'shared' : version;
    const untouched = !(key in previous[map])
      && (map !== 'shared' && baseline?.[key] != null && sameValue(value, baseline[key])
        // the drawers write their neutral value for a setting nobody set
        || isUnset(baseline?.[key]) && NEUTRAL_VALUES.includes(value));
    // the built-in page background is no choice of the administrator: nothing is stored while none is configured
    const builtInPage = key === 'pageBackgroundColor' && normalizeColor(value) === '#f0f0f0'
      && (defaultOf(key) == null || normalizeColor(defaultOf(key)) === '#f0f0f0');
    if (!isManaged(key) || isUnset(value) || value === 'none' || untouched || builtInPage
        || (map === 'shared' || version === 'light') && defaultOf(key) != null && sameValue(value, defaultOf(key))) {
      return;
    }
    settings[map][key] = String(value);
  });
  Object.keys(IMAGE_FILE_FIELDS).forEach(imageKey => {
    const file = edited.files?.[IMAGE_FILE_FIELDS[imageKey]];
    const fileKey = imageKey + FILE_SUFFIX;
    const uploadKey = imageKey + UPLOAD_SUFFIX;
    const stored = theme.settings?.[version]?.[fileKey] ?? theme.configSettings?.[version]?.[fileKey];
    delete settings[version][fileKey];
    delete settings[version][uploadKey];
    delete pendingImages[`${version}:${imageKey}`];
    if (file?.uploadId && String(file.uploadId) !== '0') {
      settings[version][uploadKey] = String(file.uploadId);
      pendingImages[`${version}:${imageKey}`] = {data: file.data};
    } else if (file?.fileId && stored && String(file.fileId) === String(stored)) {
      settings[version][fileKey] = String(stored);
    }
  });
  return {settings, pendingImages};
}

/**
 * Restores an area: every setting of the area goes back to the defaults, in both versions.
 *
 * @param {object} theme the working theme
 * @param {RegExp} pattern the keys of the area
 * @returns {object} the theme's settings without the area's keys, images included
 */
export function restoreAreaKeys(theme, pattern) {
  const settings = copySettings(theme.settings);
  Object.values(settings).forEach(map => Object.keys(map)
    .filter(key => pattern.test(key))
    .forEach(key => delete map[key]));
  return settings;
}

function diffMap(base, desired) {
  const diff = {};
  Object.keys(desired).forEach(key => {
    if (base[key] !== desired[key]) {
      diff[key] = desired[key];
    }
  });
  Object.keys(base).forEach(key => {
    if (!(key in desired)) {
      diff[key] = null;
    }
  });
  return diff;
}

/**
 * @param {object} theme a working theme
 * @returns {object} its publication: an existing theme sends its whole maps; a theme added from a stored one
 * sends only what differs from the copy the server makes (a removed key as null); a theme added from an
 * unpublished one has no server-side source and sends its whole maps
 */
export function toThemePublication(theme) {
  const publication = {name: theme.name};
  if (theme.id) {
    publication.id = theme.id;
    publication.shared = theme.settings.shared;
    publication.light = theme.settings.light;
    // the whole dark map too: a restored theme's is empty
    publication.dark = theme.settings.dark;
  } else {
    publication.clientId = theme.clientId;
    if (theme.startFrom?.id) {
      publication.startFrom = {id: theme.startFrom.id};
      publication.shared = diffMap(theme.base.shared, theme.settings.shared);
      publication.light = diffMap(theme.base.light, theme.settings.light);
      publication.dark = diffMap(theme.base.dark, theme.settings.dark);
    } else {
      Object.assign(publication, theme.settings);
    }
  }
  return publication;
}

/**
 * @param {object} theme the theme as the page holds it
 * @param {object} stored the theme's maps as the server stores them: the resolve call merges what it receives
 * over them, a removed key travels as null
 * @returns {object} the body of the resolve call
 */
export function toResolvePayload(theme, stored) {
  const payload = {name: theme.name};
  const base = theme.id ? stored : theme.startFrom?.id && theme.base;
  if (theme.id) {
    payload.id = theme.id;
  } else if (theme.startFrom?.id) {
    payload.clientId = theme.clientId;
    payload.startFrom = {id: theme.startFrom.id};
  } else {
    payload.clientId = theme.clientId;
  }
  ['shared', 'light', 'dark'].forEach(map => {
    payload[map] = base ? diffMap(base[map] || {}, theme.settings[map]) : theme.settings[map];
  });
  return payload;
}
