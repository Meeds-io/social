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

export function init(appId, name, canEdit) {
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => {
      Vue.createApp({
        data: {
          name,
          canEdit,
          settings: null,
          language: lang,
          defaultLanguage: eXo.env.portal.defaultLanguage,
          initialized: false,
        },
        computed: {
          links() {
            return this.settings?.links || null;
          },
          hasLinks() {
            return this.links?.length;
          },
          isMobile() {
            return this.$vuetify?.breakpoint?.smAndDown;
          },
        },
        created() {
          this.init().finally(() => this.initialized = true);
        },
        methods: {
          init() {
            return this.retrieveSettings();
          },
          retrieveSettings() {
            return this.$linkService.getSettings(this.name, this.language)
              .then(settings => {
                this.settings = settings;
                this.computeDefaultTranslations();
              });
          },
          computeDefaultTranslations() {
            return this.settings.links.forEach(link => {
              if (!link.name?.[this.defaultLanguage]) {
                link.name[this.defaultLanguage] = link.name['en'] || '';
              }
              if (!link.description?.[this.defaultLanguage]) {
                link.description[this.defaultLanguage] = link.description['en'] || '';
              }
            });
          }
        },
        template: `<links-app id="${appId}"></links-app>`,
        vuetify: Vue.prototype.vuetifyOptions,
        i18n,
      }, `#${appId}`, `Links Application - ${name}`);
    });
}
