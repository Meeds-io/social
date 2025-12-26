/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
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

const appId = 'spaceMembersDrawer';

const lang = eXo?.env.portal.language || 'en';
const urls = [
  `/social/i18n/locale.portlet.social.PeopleListApplication?lang=${lang}`,
  `/social/i18n/locale.portlet.social.SpaceInfosPortlet?lang=${lang}`
];

export async function openSpaceMembers(settings) {
  await initApp();
  document.dispatchEvent(
    new CustomEvent('space-members-drawer-open', { detail: settings })
  );
}

async function initApp() {
  if (window.spaceMembersDrawerAdded) {
    return;
  }
  const el = document.createElement('div');
  el.id = appId;
  document.querySelector('#vuetify-apps').append(el);

  window.spaceMembersDrawerAdded = true;

  const i18n = await exoi18n.loadLanguageAsync(lang, urls);

  return Vue.createApp({
    template: '<space-members-drawer />',
    vuetify: Vue.prototype.vuetifyOptions,
    i18n: i18n,
  }, el, 'Space Members Drawer');
}