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

import Login from './components/Login.vue';
import LoginMain from './components/LoginMain.vue';
import LoginMainTopExtensions from './components/LoginMainTopExtensions.vue';
import LoginMainBottomExtensions from './components/LoginMainBottomExtensions.vue';
import LoginSeparator from './components/LoginSeparator.vue';
import LoginProviders from './components/provider/LoginProviders.vue';
import LoginProvidersMenu from './components/provider/LoginProvidersMenu.vue';
import LoginProviderLink from './components/provider/LoginProviderLink.vue';
import LoginProviderMenuLink from './components/provider/LoginProviderMenuLink.vue';

const components = {
  'portal-login': Login,
  'portal-login-main': LoginMain,
  'portal-login-main-top-extensions': LoginMainTopExtensions,
  'portal-login-main-bottom-extensions': LoginMainBottomExtensions,
  'portal-login-providers': LoginProviders,
  'portal-login-providers-menu': LoginProvidersMenu,
  'portal-login-provider-link': LoginProviderLink,
  'portal-login-provider-menu-link': LoginProviderMenuLink,
  'portal-login-separator': LoginSeparator,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
