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
import './services.js';

// get overridden components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpaceTemplatesManagement');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

const lang = eXo?.env.portal.language || 'en';
const url = `/social/i18n/locale.portlet.SpaceTemplatesManagement?lang=${lang}`;

const appId = 'SpaceTemplatesManagement';
export function init() {
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n =>
      Vue.createApp({
        data: {
          spacesCountByTemplates: null,
          usersPermission: '*:/platform/users',
          administratorsPermission: '*:/platform/administrators',
          collator: new Intl.Collator(eXo.env.portal.language, {numeric: true, sensitivity: 'base'}),
        },
        computed: {
          isMobile() {
            return this.$vuetify.breakpoint.mobile;
          },
        },
        async created() {
          this.spacesCountByTemplates = await this.$spaceService.getSpacesCountByTemplates();
        },
        template: `<space-templates-management id="${appId}"/>`,
        vuetify: Vue.prototype.vuetifyOptions,
        i18n,
      }, `#${appId}`, 'Space Templates')
    );
}
