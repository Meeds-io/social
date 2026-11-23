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

// The viewer's search and advanced filter as the page URL carries them, the
// same names as the directory's users resource: one parameter set per page,
// read by every directory on it. The directory's own filter, its groups and
// the 'My connections' quick filter are never written.
const KEYWORD_PARAMETER = 'q';
const ATTRIBUTE_PARAMETER_PREFIX = 'attr.';

/**
 * @returns {{keyword: string, attributes: Object<string, string>}} the
 *          search text, null when absent, and the first non-blank value of
 *          each attribute
 */
export function readUrlFilters() {
  const params = new URLSearchParams(window.location.search);
  const attributes = {};
  params.forEach((value, name) => {
    if (name.startsWith(ATTRIBUTE_PARAMETER_PREFIX)) {
      const propertyName = name.substring(ATTRIBUTE_PARAMETER_PREFIX.length);
      if (propertyName && value?.trim() && !attributes[propertyName]) {
        attributes[propertyName] = value;
      }
    }
  });
  return {
    keyword: params.get(KEYWORD_PARAMETER)?.trim() || null,
    attributes,
  };
}

/**
 * Replaces the search and advanced filter parameters of the page URL, keeping
 * its other parameters and its hash: no reload, no new history entry. An
 * attribute the directory cannot filter on is kept: another directory of the
 * page may apply it.
 *
 * @param {string} keyword the search text
 * @param {Object<string, string>} attributes the advanced filter values
 * @param {string[]} ownAttributes the attributes the directory can filter on
 * @returns {void}
 */
export function writeUrlFilters(keyword, attributes, ownAttributes) {
  const params = new URLSearchParams(window.location.search);
  const replaced = new Set([...(ownAttributes || []), ...Object.keys(attributes || {})]
    .map(name => `${ATTRIBUTE_PARAMETER_PREFIX}${name}`));
  Array.from(params.keys())
    .filter(name => name === KEYWORD_PARAMETER || replaced.has(name))
    .forEach(name => params.delete(name));
  if (keyword?.trim()) {
    params.set(KEYWORD_PARAMETER, keyword);
  }
  Object.keys(attributes || {})
    .filter(name => attributes[name]?.trim?.())
    .forEach(name => params.set(`${ATTRIBUTE_PARAMETER_PREFIX}${name}`, attributes[name]));
  const search = params.toString();
  const url = `${window.location.pathname}${search ? `?${search}` : ''}${window.location.hash}`;
  if (url !== `${window.location.pathname}${window.location.search}${window.location.hash}`) {
    window.history.replaceState(window.history.state, '', url);
  }
}
