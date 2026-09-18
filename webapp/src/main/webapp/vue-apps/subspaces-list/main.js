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
import './initComponents.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SubspacesList');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

//getting language of user
const lang = eXo?.env?.portal?.language || 'en';

// Portlets carries this widget's own labels; the two others are the bundles of
// the shared components it reuses — <space-avatar> reads spacesList.* and
// <space-creation-button> reads spacesList.button.add and
// menu.spaces.addNewSpaceTooltip.
const urls = [
  `/social/i18n/locale.portlet.Portlets?lang=${lang}`,
  `/social/i18n/locale.portlet.social.SpacesListApplication?lang=${lang}`,
  `/social/i18n/locale.portal.HamburgerMenu?lang=${lang}`,
];

/**
 * Bootstraps the widget from the portlet preferences and URLs the JSP
 * passes: {appId, headerTranslations, showHiddenSubspaces, subspacesLimit,
 * saveSettingsUrl, resourceUrl}. Every business decision (parent space or
 * not, what to list, who may create or manage) comes from the portlet
 * resource call, never from here.
 *
 * @param {object} settings preferences and URLs read by the JSP
 * @returns {void}
 */
export function init(settings) {
  exoi18n.loadLanguageAsync(lang, urls)
    .then(i18n => {
      Vue.createApp({
        data: {
          settings,
          spaceId: eXo?.env?.portal?.spaceId,
          language: lang,
          defaultLanguage: eXo?.env?.portal?.defaultLanguage,
          loading: false,
        },
        computed: {
          headerTitle() {
            return this.settings?.headerTranslations?.[this.language]
              || this.settings?.headerTranslations?.[this.defaultLanguage];
          },
        },
        template: `<subspaces-list id="${settings.appId}" />`,
        i18n,
        vuetify: Vue.prototype.vuetifyOptions,
      }, `#${settings.appId}`, 'Subspaces List');
    });
}
