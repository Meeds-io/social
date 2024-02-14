/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
import GeneralSettings from './components/GeneralSettings.vue';

import LoginBranding from './components/login-page/LoginBranding.vue';

import ColorPicker from './components/branding/form/ColorPicker.vue';
import CompanyLogo from './components/branding/form/CompanyLogo.vue';
import CompanyFavicon from './components/branding/form/CompanyFavicon.vue';
import LoginBackgroundSelector from './components/branding/form/LoginBackgroundSelector.vue';
import BorderRadiusSelector from './components/branding/form/BorderRadiusSelector.vue';
import SiteBranding from './components/branding/SiteBranding.vue';
import SiteBrandingWindow from './components/branding/SiteBrandingWindow.vue';
import SiteBrandingPreview from './components/branding/preview/SiteBrandingPreview.vue';

import HubAccess from './components/registration/HubAccess.vue';
import DefaultSpacesDrawer from './components/registration/DefaultSpacesDrawer.vue';

import PublicSiteEditDrawer from './components/public-site/PublicSiteEditDrawer.vue';

const components = {
  'portal-general-settings': GeneralSettings,
  'portal-general-settings-branding-site-window': SiteBrandingWindow,
  'portal-general-settings-branding-site': SiteBranding,
  'portal-general-settings-branding-site-preview': SiteBrandingPreview,
  'portal-general-settings-branding-login': LoginBranding,
  'portal-general-settings-color-picker': ColorPicker,
  'portal-general-settings-company-logo': CompanyLogo,
  'portal-general-settings-company-favicon': CompanyFavicon,
  'portal-general-settings-border-radius': BorderRadiusSelector,
  'portal-general-settings-login-background-selector': LoginBackgroundSelector,
  'portal-general-settings-hub-access': HubAccess,
  'portal-general-settings-default-spaces-drawer': DefaultSpacesDrawer,
  'portal-general-settings-public-site-drawer': PublicSiteEditDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
