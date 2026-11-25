/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
import UserSettingHiddenApplications from './components/UserSettingHiddenApplications.vue';
import UserSettingHiddenApplicationsDrawer from './components/UserSettingHiddenApplicationsDrawer.vue';

const components = {
  'user-setting-hidden-applications': UserSettingHiddenApplications,
  'user-setting-hidden-applications-drawer': UserSettingHiddenApplicationsDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

const lang = eXo.env.portal.language;
const url = `/social/i18n/locale.portlet.social.UserSettings?lang=${lang}`;
const appId = 'UserSettingHiddenApplications';

export function init() {
  exoi18n.loadLanguageAsync(lang, url)
    .then(i18n => {
      Vue.createApp({
        template: `<user-setting-hidden-applications id="${appId}" />`,
        vuetify: Vue.prototype.vuetifyOptions,
        i18n,
      }, `#${appId}`, 'User Setting Hidden Applications');
    });
}
