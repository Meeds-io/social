/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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

// Background margin/radius (EXIP-88427) can't be stored as dedicated ModelStyle
// fields since ModelStyle is an external org.exoplatform.portal class we can't
// extend from this repo, so the 8 values are opaquely encoded as cssClass tokens
// (same pattern already used to mirror marginTop/radiusTopRight into mt-/brtr-
// classes, under a prefix that can't collide with those).
const BACKGROUND_LAYER_TOKEN_PREFIXES = {
  marginTop: 'layout-bg-margin-top',
  marginRight: 'layout-bg-margin-right',
  marginBottom: 'layout-bg-margin-bottom',
  marginLeft: 'layout-bg-margin-left',
  radiusTopRight: 'layout-bg-radius-tr',
  radiusTopLeft: 'layout-bg-radius-tl',
  radiusBottomRight: 'layout-bg-radius-br',
  radiusBottomLeft: 'layout-bg-radius-bl',
};

export function parseBackgroundLayerValues(cssClass) {
  if (!cssClass) {
    return null;
  }
  const values = {};
  let found = false;
  Object.entries(BACKGROUND_LAYER_TOKEN_PREFIXES).forEach(([key, prefix]) => {
    const match = cssClass.match(new RegExp(`(?:^| )${prefix}-([0-9]+)(?: |$)`));
    if (match) {
      values[key] = parseInt(match[1]);
      found = true;
    }
  });
  return found ? values : null;
}

export function setBackgroundLayerValues(container, partialValues) {
  // Merge with whatever's already encoded so that the margin component's
  // writes never clobber the radius component's tokens (and vice-versa) -
  // each only knows about its own 4 values, not the other's.
  const values = { ...parseBackgroundLayerValues(container.cssClass), ...partialValues };
  const prefixesPattern = Object.values(BACKGROUND_LAYER_TOKEN_PREFIXES).join('|');
  let cssClass = (container.cssClass || '').replace(new RegExp(`(^| )(${prefixesPattern})-[0-9]+`, 'g'), '').replace(/ {2,}/g, ' ').trim();
  Object.entries(BACKGROUND_LAYER_TOKEN_PREFIXES).forEach(([key, prefix]) => {
    if (values[key] || values[key] === 0) {
      cssClass += ` ${prefix}-${values[key]}`;
    }
  });
  container.cssClass = cssClass.trim();
}
