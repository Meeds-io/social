/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

import './initComponents.js';
import './services.js';

if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('Links');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

const lang = eXo.env.portal.language;
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.Links-${lang}.json`;

export function init(appId, name, canEdit, nameExists, saveSettingsUrl) {
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => {
      Vue.createApp({
        data: {
          name,
          canEdit,
          settings: null,
          language: lang,
          defaultLanguage: 'en',
          initialized: false,
          iconMaxWidthRatio: 1.3,
        },
        computed: {
          links() {
            return this.settings?.links || null;
          },
        },
        created() {
          this.init().finally(() => this.initialized = true);
        },
        methods: {
          init() {
            if (!nameExists && canEdit) {
              return this.saveSettingName();
            } else {
              return this.retrieveSettings()
                .then(() => {
                  if (!this.settings) {
                    return this.saveSettingName();
                  }
                });
            }
          },
          saveSettingName() {
            return Vue.prototype.$linkService.saveSettingName(saveSettingsUrl, name)
              .then(() => this.nameExists = true)
              .then(() => this.retrieveSettings())
              .catch((e) => console.error('Error saving settings', e));
          },
          retrieveSettings() {
            return this.$linkService.getSettings(this.name, this.language)
              .then(settings => this.settings = settings);
          },
        },
        template: `<links-app id="${appId}"></links-app>`,
        vuetify: Vue.prototype.vuetifyOptions,
        i18n,
      }, `#${appId}`, `Links Application - ${name}`);
    });
}