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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

/**
 * The SHARED/mermaid module (EXO-93576): loads the Mermaid split ES-module
 * build this webapp ships, on the first call only, never from the internet,
 * and hands out Mermaid behind settings no caller can loosen: strict
 * security (labels as text, no click handler, no script), SVG text labels
 * rather than HTML ones, no rendering on page load, and bounded sources. A
 * caller sets its own theme, fonts and layouts with initialize(): any other
 * setting it passes is dropped, and the safe settings always win.
 */
var mermaidLoader = (function() {
  var MERMAID_VERSION = '11.17.2';
  var MERMAID_URL = '/social/js/lib/mermaid-' + MERMAID_VERSION + '/mermaid.esm.min.mjs';
  /** The settings a caller can't change, nor a diagram through a directive. */
  var SAFE_SETTINGS = {
    startOnLoad: false,
    securityLevel: 'strict',
    htmlLabels: false,
    flowchart: {htmlLabels: false},
    maxTextSize: 50000,
    maxEdges: 120,
    suppressErrorRendering: true,
    secure: ['secure', 'securityLevel', 'startOnLoad', 'maxTextSize', 'maxEdges', 'suppressErrorRendering', 'theme',
      'themeVariables', 'themeCSS', 'fontFamily', 'altFontFamily', 'darkMode', 'htmlLabels', 'flowchart', 'look',
      'handDrawnSeed', 'deterministicIds', 'dompurifyConfig'],
  };
  /**
   * The settings a caller may set: its theme, its fonts and each diagram
   * kind's layout. Any other key, such as a sanitizer configuration or a
   * stylesheet, is never passed to Mermaid.
   */
  var CALLER_SETTINGS = ['theme', 'themeVariables', 'fontFamily', 'altFontFamily', 'darkMode', 'look', 'flowchart',
    'sequence', 'gantt', 'timeline', 'mindmap', 'state', 'class', 'er', 'journey', 'pie'];
  var loading = null;

  /**
   * @param {Object} settings a caller's settings
   * @returns {Object} those of them a caller may set, the safe settings
   *   applied over them
   */
  function withSafeSettings(settings) {
    var allowed = {};
    CALLER_SETTINGS.forEach(function(key) {
      if (settings && Object.prototype.hasOwnProperty.call(settings, key)) {
        allowed[key] = settings[key];
      }
    });
    var merged = Object.assign(allowed, SAFE_SETTINGS);
    merged.flowchart = Object.assign({}, (settings && settings.flowchart) || {}, SAFE_SETTINGS.flowchart);
    return merged;
  }

  /**
   * @param {Object} mermaid the Mermaid API
   * @returns {Object} the API this module hands out
   */
  function wrap(mermaid) {
    mermaid.initialize(withSafeSettings({}));
    return {
      version: MERMAID_VERSION,
      /**
       * @param {Object} settings the caller's settings (theme, fonts…)
       */
      initialize: function(settings) {
        mermaid.initialize(withSafeSettings(settings));
      },
      /**
       * @param {string} text a diagram's Mermaid text
       * @returns {Promise} resolved when it parses, rejected otherwise
       */
      parse: function(text) {
        return mermaid.parse(text);
      },
      /**
       * @param {string} id the SVG's id, unique in the page
       * @param {string} text a diagram's Mermaid text
       * @param {Element} container where Mermaid lays it out
       * @returns {Promise<{svg: string}>} the drawn SVG
       */
      render: function(id, text, container) {
        return mermaid.render(id, text, container);
      },
    };
  }

  return {
    version: MERMAID_VERSION,
    /**
     * Loads Mermaid once; a failed load is tried again on the next call.
     *
     * @returns {Promise<Object>} the API, see wrap()
     */
    load: function() {
      if (!loading) {
        loading = import(/* webpackIgnore: true */ MERMAID_URL)
          .then(function(module) {
            return wrap(module.default);
          })
          .catch(function(error) {
            loading = null;
            throw error;
          });
      }
      return loading;
    },
  };
})();
