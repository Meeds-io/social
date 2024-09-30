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

// getting language of the PLF 
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpaceManagersApplication');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

const appId = 'SpaceManagersApplication';
const url = `/social-portlet/i18n/locale.portlet.social.SpaceInfosPortlet?lang=${lang}`;

export async function init(spaceId, isManager, isMember, managers) {
  if (!isManager && !managers.length) {
    Vue.prototype.$updateApplicationVisibility(false, document.querySelector(`#${appId}`));
    return;
  }
  const i18n = await exoi18n.loadLanguageAsync(lang, url);
  Vue.createApp({
    data: {
      spaceId,
      isManager,
      isMember,
      managers,
    },
    template: `<space-managers-application id="${appId}" />`,
    vuetify: Vue.prototype.vuetifyOptions,
    i18n,
  }, `#${appId}`, 'Space Managers');
}
