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
// The users management drawers, so that a user is created from the page
// holding this application, with the same steps and outcomes as on the users
// management page
import '../idm-users-management/initComponents.js';
import '../idm-users-management/services.js';
import Users from './components/Users.vue';
import UsersDrawer from './components/UsersDrawer.vue';
import AvatarsSeeAll from '../platformAdminsWidget/components/AvatarsSeeAll.vue';

const components = {
  'platform-users': Users,
  'platform-users-drawer': UsersDrawer,
  'platform-avatars-see-all': AvatarsSeeAll,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

const lang = eXo?.env?.portal?.language || 'en';
const urls = [
  `/social/i18n/locale.portlet.PlatformAdminsWidget?lang=${lang}`,
  `/social/i18n/locale.portlet.Portlets?lang=${lang}`,
  `/social/i18n/locale.portlet.UsersManagement?lang=${lang}`,
];
const appId = 'PlatformUsersWidget';

export function init() {
  exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
    Vue.createApp({
      data: {
        isSuperUser: false,
        presetStatus: 'ENABLED',
      },
      computed: {
        isMobile() {
          return this.$vuetify.breakpoint.mobile;
        },
      },
      async created() {
        const superUser = await this.$userService.isSuperUser();
        this.isSuperUser = superUser.isSuperUser === 'true';
      },
      template: `<platform-users id="${appId}" />`,
      vuetify: Vue.prototype.vuetifyOptions,
      i18n,
    }, `#${appId}`, 'Platform Users Widget');
  });
}
