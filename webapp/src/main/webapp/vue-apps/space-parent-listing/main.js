/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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
  const components = extensionRegistry.loadComponents('ParentSpaceListing');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API
const url = `/social/i18n/locale.portlet.Portlets?lang=${lang}`;

export function init(appId, parentSpaceId, isManager, settings, saveSettingsUrl) {
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => {
      if (!settings.headerTranslations) {
        settings.headerTranslations = {};
      }
      Vue.createApp({
        data: {
          parentSpaceId: parentSpaceId,
          isManager: isManager,
          settings: settings,
          saveSettingsUrl: saveSettingsUrl,
          space: null,
          defaultLanguage: eXo?.env?.portal?.defaultLanguage,
          loading: false
        },
        async created() {
          this.loading = true;
          this.space = await this.$spaceService.getSpaceById(this.parentSpaceId);
          this.loading = false;
        },
        computed: {
          headerTitle() {
            return this.settings?.headerTranslations?.[lang] || this.settings?.headerTranslations?.[this.defaultLanguage];
          }
        },
        template: `<parent-space id="${appId}" />`,
        i18n,
        vuetify: Vue.prototype.vuetifyOptions,
      }, `#${appId}`, 'Parent space listing');
    });
}
