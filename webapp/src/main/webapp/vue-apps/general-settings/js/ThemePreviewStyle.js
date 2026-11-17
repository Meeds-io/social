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

import {IMAGE_FILE_FIELDS, FILE_SUFFIX, UPLOAD_SUFFIX} from './ThemeSettingsMapper.js';

/**
 * The style the admin preview injects into its iframe: the resolved values of
 * a theme as the --allPages* custom properties the branding stylesheet
 * (portal branding.less) declares.
 */

// resolved key -> custom property, where the stylesheet's name is not --allPages + the key
const PROPERTY_NAMES = Object.freeze({
  textColor: '--allPagesBaseTextColor',
  primaryHoverBackgroundColor: '--allPagesBtnPrimaryHoverBackgroundColor',
  commentBubbleColor: '--allPagesCommentBackgroundColor',
  drawerCommentBubbleColor: '--allPagesDrawerCommentBackgroundColor',
  pageWidth: '--allPagesWidth',
  pageBackgroundColor: '--allPagesBackgroundColor',
  pageBackgroundImage: '--allPagesBackgroundImage',
  pageBackgroundSize: '--allPagesBackgroundSize',
  pageBackgroundPosition: '--allPagesBackgroundPosition',
  pageBackgroundRepeat: '--allPagesBackgroundRepeat',
  pageBackgroundAttachment: '--allPagesBackgroundAttachment',
});

// the server renders these on <body>: the preview declares them there too
const BODY_NAMES = Object.freeze(['Width', 'BackgroundColor', 'BackgroundImage', 'BackgroundSize', 'BackgroundPosition',
  'BackgroundRepeat', 'BackgroundAttachment'].map(name => `--allPages${name}`));
const BODY_PROPERTIES = /^--allPages(Width|Background(Color|Image|Size|Position|Repeat|Attachment))$/;

const upperFirst = text => text.charAt(0).toUpperCase() + text.slice(1);

export function propertyName(key) {
  if (PROPERTY_NAMES[key]) {
    return PROPERTY_NAMES[key];
  }
  if (key.endsWith(FILE_SUFFIX) || key.endsWith(UPLOAD_SUFFIX) || key === 'name') {
    return null;
  }
  // the page margins are --allPagesMargin*, the application ones --allPagesAppMargin*
  return `--allPages${upperFirst(key.replace(/^page(No)?Margin/, '$1Margin'))}`;
}

function hasEffect(value) {
  return value && value !== 'none' && value !== 'initial';
}

// the image the version shows: an unsaved upload, else the stored file, layered before the effect
function imageValue(key, values, theme, version, themeId) {
  const own = theme?.settings?.[version] || {};
  const shared = version === 'dark' && !own[key + FILE_SUFFIX] && !own[key + UPLOAD_SUFFIX] ? 'light' : version;
  const source = theme?.settings?.[shared] || {};
  const upload = source[key + UPLOAD_SUFFIX];
  const fileId = values[key + FILE_SUFFIX];
  const layers = [];
  if (upload && theme.pendingImages?.[`${shared}:${key}`]?.data) {
    layers.push(`url(${theme.pendingImages[`${shared}:${key}`].data})`);
  } else if (fileId && themeId) {
    layers.push(`url(${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/platform/branding/themes/${themeId}/images/${key}`
      + `?version=${version}&v=${fileId})`);
  }
  if (hasEffect(values[key])) {
    layers.push(values[key]);
  }
  return layers.join(', ') || 'none';
}

/**
 * @param {object} values the resolved values of a version
 * @param {object} theme the previewed theme, for its unsaved uploads
 * @param {string} version light or dark
 * @param {number} themeId the id the theme's images are served under
 * @returns {object} custom property name to value
 */
export function toCustomProperties(values, theme, version, themeId) {
  const properties = {};
  Object.keys(values || {}).forEach(key => {
    const name = propertyName(key);
    const value = values[key];
    if (!name || value == null || value === '') {
      return;
    }
    properties[name] = Object.prototype.hasOwnProperty.call(IMAGE_FILE_FIELDS, key)
      ? imageValue(key, values, theme, version, themeId)
      : value;
  });
  if (values?.secondaryColor) {
    properties['--allPagesTertiaryColor'] = values.secondaryColor;
  }
  if (values?.primaryColor) {
    properties['--allPagesUiSwitchOnColor'] = values.primaryColor;
  }
  if (values?.commentBubbleColor) {
    properties['--themeCommentBackgroundColor'] = values.commentBubbleColor;
  }
  return properties;
}

const declarations = (properties, important) => Object.keys(properties)
  .map(name => `${name}: ${properties[name]}${important ? ' !important' : ''};`)
  .join('\n  ');

/**
 * @param {object} resolved the resolved theme: light and dark values
 * @param {string} version the version previewed
 * @param {object} theme the previewed theme as the page holds it
 * @param {number} themeId the id the theme's images are served under
 * @returns {string} the stylesheet: the previewed version on :root and under its attribute, the edited light
 * values for a light scope, the page background where the server declares it
 */
export function buildPreviewStyle(resolved, version, theme, themeId) {
  const previewed = toCustomProperties(resolved?.[version], theme, version, themeId);
  const light = toCustomProperties(resolved?.light, theme, 'light', themeId);
  const body = {};
  const root = {};
  Object.keys(previewed).forEach(name => (BODY_PROPERTIES.test(name) ? body : root)[name] = previewed[name]);
  // the page's own properties are always declared: an absent one must not leave the theme in use's value in place
  BODY_NAMES.forEach(name => body[name] = body[name] ?? 'initial');
  if (!previewed['--allPagesBackgroundColor'] && version === 'dark') {
    // the server's dark page background (portal ThemeColorCalculator.DARK_PAGE_BACKGROUND, BrandingServiceImpl#compileThemeVersions)
    body['--allPagesBackgroundColor'] = '#14181B';
  }
  return `:root, :root[data-display-mode="${version}"] {\n  ${declarations(root)}\n}\n`
    + `[data-display-mode="light"]:not(:root) {\n  ${declarations(light)}\n}\n`
    + `body {\n  ${declarations(body, true)}\n}\n`;
}
