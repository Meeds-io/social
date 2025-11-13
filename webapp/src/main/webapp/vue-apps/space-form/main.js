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

// get overridden components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpaceTemplatesManagement');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

const appId = 'spaceFormDrawer';
const lang = eXo?.env.portal.language || 'en';
const url = `/social/i18n/locale.portlet.social.SpacesListApplication?lang=${lang}`;

export async function open(templateId, spaceTemplates, parentSpaceId) {
  if (!window.spaceFormAdded) {
    await initApp(spaceTemplates);
  }
  document.dispatchEvent(new CustomEvent('addNewSpace', {detail: {
    templateId: templateId,
    spaceTemplates: spaceTemplates,
    parentSpaceId: parentSpaceId
  }}));
}

export async function edit(spaceId) {
  if (!window.spaceFormAdded) {
    await initApp();
  }
  const space = await Vue.prototype.$spaceService.getSpaceById(spaceId, Date.now());
  document.dispatchEvent(new CustomEvent('editSpace', {detail: space}));
}

function initApp(spaceTemplates) {
  const spaceFormElement = document.createElement('div');
  spaceFormElement.setAttribute('id', appId);
  document.querySelector('#vuetify-apps').append(spaceFormElement);
  return new Promise(resolve => exoi18n.loadLanguageAsync(lang, url)
    .then(i18n =>
      Vue.createApp({
        data: {
          isExternalFeatureEnabled: eXo.env.portal.isExternalFeatureEnabled,
          spaceTemplates,
          collator: new Intl.Collator(eXo.env.portal.language, {
            numeric: true,
            sensitivity: 'base'
          }),
        },
        computed: {
          isMobile() {
            return this.$vuetify.breakpoint.mobile;
          },
        },
        mounted() {
          resolve();
        },
        template: '<space-form-drawer />',
        vuetify: Vue.prototype.vuetifyOptions,
        i18n,
      }, spaceFormElement, 'Space Form')
    ));
}