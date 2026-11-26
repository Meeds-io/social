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

// The contrast ratio the server calculates for a theme's text colors (portal ThemeColorCalculator#contrast, WCAG 2.x),
// for a color being edited before the server resolves it: the alpha of either color is ignored, as the server ignores it

export const READABLE_RATIO = 4.5;

// the two colors an Auto text takes, per level (portal TextLevel): the one that reads best on its background
const TEXT_LEVELS = Object.freeze({
  neutral: ['#20282C', '#E6E9EB'],
  grey: ['#6B6B6B', '#9BA3A8'],
});

const HEX_COLOR = /^#([0-9a-f]{3,4}|[0-9a-f]{6}|[0-9a-f]{8})$/i;

/**
 * @param {string} color a color
 * @returns {boolean} true for #RGB, #RGBA, #RRGGBB or #RRGGBBAA
 */
export function isHexColor(color) {
  return HEX_COLOR.test(String(color || '').trim());
}

/**
 * @param {string} color a hex color
 * @returns {string} its #RRGGBBAA form, upper case, opaque when it has no alpha
 */
export function normalizeColor(color) {
  let hex = String(color || '').trim().replace('#', '').toUpperCase();
  if (hex.length === 3 || hex.length === 4) {
    hex = hex.split('').map(c => c + c).join('');
  }
  return `#${hex.length === 6 ? `${hex}FF` : hex}`;
}

/**
 * @param {string} first a color
 * @param {string} second a color
 * @returns {boolean} true when both are the same hex color, whatever their shape
 */
export function sameColor(first, second) {
  return isHexColor(first) && isHexColor(second) && normalizeColor(first) === normalizeColor(second);
}

/**
 * @param {string} color a hex color
 * @returns {string} its #RRGGBB form, its alpha dropped
 */
export function opaqueColor(color) {
  return normalizeColor(color).substring(0, 7);
}

/**
 * @param {string} color a hex color
 * @param {string} background a hex color
 * @returns {number} the contrast ratio of the two colors, from 1 to 21, null when either is not a hex color
 */
export function contrastRatio(color, background) {
  if (!isHexColor(color) || !isHexColor(background)) {
    return null;
  }
  const first = luminance(color);
  const second = luminance(background);
  return (Math.max(first, second) + 0.05) / (Math.min(first, second) + 0.05);
}

/**
 * The color the server gives an Auto text on a background (portal ThemeColorCalculator#autoText): of the level's two
 * colors, the one with the higher contrast, the first on a tie.
 *
 * @param {string} level neutral (titles, body text) or grey (headers, subtitles, icons)
 * @param {string} background a hex color
 * @returns {string} the Auto color, null when the background is not a hex color
 */
export function autoTextColor(level, background) {
  const [first, second] = TEXT_LEVELS[level] || TEXT_LEVELS.neutral;
  if (!isHexColor(background)) {
    return null;
  }
  return contrastRatio(first, background) >= contrastRatio(second, background) ? first : second;
}

function luminance(color) {
  const hex = normalizeColor(color);
  const [r, g, b] = [1, 3, 5].map(index => linear(parseInt(hex.substring(index, index + 2), 16) / 255));
  return 0.2126 * r + 0.7152 * g + 0.0722 * b;
}

function linear(channel) {
  return channel <= 0.04045 ? channel / 12.92 : Math.pow((channel + 0.055) / 1.055, 2.4);
}
