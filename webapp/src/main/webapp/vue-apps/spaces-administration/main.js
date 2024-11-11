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
import '../space-form/initComponents.js';
import './initComponents.js';
import './extensions.js';
import './services.js';

if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpacesAdministration');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

export function init() {
  const appId = 'spacesAdministration';
  const lang = eXo?.env?.portal?.language || 'en';
  exoi18n.loadLanguageAsync(lang, [
    `/social/i18n/locale.portlet.social.SpacesAdministrationPortlet?lang=${lang}`,
    `/social/i18n/locale.portlet.social.SpacesListApplication?lang=${lang}`,
    `/social/i18n/locale.portlet.SpaceTemplatesManagement?lang=${lang}`,
    `/social/i18n/locale.portal.webui?lang=${lang}`,
  ])
    .then(i18n => Vue.createApp({
      data: {
        spaceTemplates: null,
        mainExtensions: [],
        tableColumnExtensions: [],
        itemMenuExtensions: [],
        usersPermission: '/platform/users',
        externalsPermission: '/platform/externals',
        administratorsPermission: '/platform/administrators',
        collator: new Intl.Collator(eXo.env.portal.language, {numeric: true, sensitivity: 'base'}),
      },
      computed: {
        isMobile() {
          return this.$vuetify.breakpoint.mobile;
        },
      },
      created() {
        document.addEventListener('extension-spaces-administration-menu-action-updated', this.refreshExtensions);
        document.addEventListener('extension-spaces-administration-main-updated', this.refreshExtensions);
        document.addEventListener('extension-spaces-administration-table-column-updated', this.refreshExtensions);
        this.refreshSpaceTemplates();
        this.refreshExtensions();
      },
      beforeDestroy() {
        document.removeEventListener('extension-spaces-administration-menu-action-updated', this.refreshExtensions);
        document.removeEventListener('extension-spaces-administration-main-updated', this.refreshExtensions);
        document.removeEventListener('extension-spaces-administration-table-column-updated', this.refreshExtensions);
      },
      methods: {
        async refreshSpaceTemplates() {
          this.spaceTemplates = await this.$spaceTemplateService.getSpaceTemplates(true);
        },
        refreshExtensions() {
          this.itemMenuExtensions = extensionRegistry.loadExtensions('spaces-administration', 'menu-action') || [];
          this.mainExtensions = extensionRegistry.loadExtensions('spaces-administration', 'main') || [];
          this.tableColumnExtensions = extensionRegistry.loadExtensions('spaces-administration', 'table-column') || [];
        },
      },
      template: `<spaces-administration id="${appId}" />`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'Spaces Administration'))
    .finally(() => Vue.prototype.$utils.includeExtensions('ManageSpacesExtension'));
}