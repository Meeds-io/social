/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
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

const appId = 'loginFormApplication';

//getting language of the PLF
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

//should expose the locale ressources as REST API 
const urls = [
  `/social/i18n/locale.portlet.Login?lang=${lang}`,
  `/social/i18n/locale.portal.login?lang=${lang}`
];

export function init(params) {
  const jsonParams = JSON.parse(params);
  exoi18n.loadLanguageAsync(lang, urls).then(i18n => {
    // init Vue app when locale ressources are ready
    Vue.createApp({
      data: {
        portletStorageId: jsonParams.portletStorageId,
        pageRef: jsonParams.pageRef,
        params: params,
      },
      template: `<login-form id="${appId}" :params="params"/>`,
      vuetify: Vue.prototype.vuetifyOptions,
      i18n
    }, `#${appId}`, 'Login Form');
  }).finally(() => Vue.prototype.$utils.includeExtensions('LoginExtension'));
}
