/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
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
import './extensions.js';

Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

const appId = 'attachmentPreview';

//getting language of the PLF
const lang = eXo?.env?.portal?.language || 'en';

//should expose the locale ressources as REST API
const url = `/social/i18n/locale.portlet.Portlets?lang=${lang}`;

export function init(event) {
  if (!document.querySelector(`#${appId}`)) {
    const parent = document.createElement('div');
    parent.id = appId;
    document.querySelector('#vuetify-apps').appendChild(parent);
    exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale ressources are ready
      Vue.createApp({
        template: `<attachments-preview-dialog id="${appId}"/>`,
        vuetify,
        i18n,
        mounted() {
          document.dispatchEvent(new CustomEvent('open-preview-dialog', event));
        },
      }, `#${appId}`, 'Attachments Preview application');

    });
  } else {
    document.dispatchEvent(new CustomEvent('open-preview-dialog', event));
  }
  Vue.prototype.$utils.includeExtensions('Preview');
}