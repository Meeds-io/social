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

import LoginForm from './components/LoginForm.vue';
import LoginSeparator from '../login/components/LoginSeparator.vue';
import LoginProviders from '../login/components/provider/LoginProviders.vue';
import LoginProvidersMenu from '../login/components/provider/LoginProvidersMenu.vue';
import LoginProviderLink from '../login/components/provider/LoginProviderLink.vue';
import LoginProviderMenuLink from '../login/components/provider/LoginProviderMenuLink.vue';

const components = {
  'login-form': LoginForm,
  'portal-login-providers': LoginProviders,
  'portal-login-providers-menu': LoginProvidersMenu,
  'portal-login-provider-link': LoginProviderLink,
  'portal-login-provider-menu-link': LoginProviderMenuLink,
  'portal-login-separator': LoginSeparator,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
