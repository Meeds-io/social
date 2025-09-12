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
import BackgroundImageAttachment from './components/branding/form/BackgroundImageAttachment.vue';
import BackgroundInput from './components/branding/form/BackgroundInput.vue';
import CustomStyleInput from './components/branding/form/CustomStyleInput.vue';

import SiteBranding from './components/branding/SiteBranding.vue';
import SiteBrandingWindow from './components/branding/SiteBrandingWindow.vue';
import SiteBrandingPreview from './components/branding/preview/SiteBrandingPreview.vue';

import PublicSiteEditDrawer from './components/public-site/PublicSiteEditDrawer.vue';
import DefaultLanguageDrawer from './components/language/DefaultLanguageDrawer.vue';

import NavigationSettings from './components/navigation/NavigationSettings.vue';

import NavigationSettingsTopbar from './components/navigation/topbar/NavigationSettingsTopbar.vue';
import NavigationSettingsTopbarPreview from './components/navigation/topbar/NavigationSettingsTopbarPreview.vue';
import NavigationSettingsTopbarApplicationDrawer from './components/navigation/topbar/NavigationSettingsTopbarApplicationDrawer.vue';

import NavigationSettingsSidebar from './components/navigation/sidebar/NavigationSettingsSidebar.vue';
import NavigationSettingsSidebarPreview from './components/navigation/sidebar/NavigationSettingsSidebarPreview.vue';
import NavigationSettingsSidebarPreviewItem from './components/navigation/sidebar/NavigationSettingsSidebarPreviewItem.vue';
import NavigationSettingsSidebarAddButton from './components/navigation/sidebar/NavigationSettingsSidebarAddButton.vue';
import NavigationSettingsIcon from './components/navigation/sidebar/NavigationSettingsIcon.vue';
import NavigationSettingsIconInput from './components/navigation/sidebar/NavigationSettingsIconInput.vue';

import NavigationSettingsAddSidebarLinkDrawer from './components/navigation/sidebar/drawer/NavigationSettingsAddSidebarLinkDrawer.vue';
import NavigationSettingsAddSidebarSiteDrawer from './components/navigation/sidebar/drawer/NavigationSettingsAddSidebarSiteDrawer.vue';
import NavigationSettingsAddSidebarSpacesDrawer from './components/navigation/sidebar/drawer/NavigationSettingsAddSidebarSpacesDrawer.vue';

import SiteBrandingOptions from './components/branding/options/SiteBrandingOptions.vue';
import SiteBrandingOptionsItem from './components/branding/options/SiteBrandingOptionsItem.vue';
import UpdateColorsDrawer from './components/branding/drawers/UpdateColorsDrawer.vue';
import TopBarStylingDrawer from './components/branding/drawers/TopBarStylingDrawer.vue';
import TextInput from './components/branding/form/TextInput.vue';
import SideBarStylingDrawer from './components/branding/drawers/SideBarStylingDrawer.vue';
import DrawerStyling from './components/branding/drawers/DrawerStyling.vue';
import PageStylingDrawer from './components/branding/drawers/PageStylingDrawer.vue';

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
  'portal-general-settings-branding-options': SiteBrandingOptions,
  'portal-general-settings-branding-options-item': SiteBrandingOptionsItem,
  'portal-general-settings-branding-update-colors-drawer': UpdateColorsDrawer,
  'portal-general-settings-branding-top-bar-styling-drawer': TopBarStylingDrawer,
  'portal-general-settings-branding-sidebar-styling-drawer': SideBarStylingDrawer,
  'portal-general-settings-branding-drawer-styling': DrawerStyling,
  'portal-general-settings-branding-page-styling-drawer': PageStylingDrawer,
  'portal-general-settings-branding-text-input': TextInput,
  'portal-general-settings-background-image-attachment': BackgroundImageAttachment,
  'portal-general-settings-background-input': BackgroundInput,
  'portal-general-settings-custom-style-input': CustomStyleInput,
  'portal-general-settings-login-background-selector': LoginBackgroundSelector,
  'portal-general-settings-public-site-drawer': PublicSiteEditDrawer,
  'portal-general-settings-default-language-drawer': DefaultLanguageDrawer,
  'portal-general-settings-navigation-settings': NavigationSettings,
  'portal-general-settings-navigation-settings-topbar': NavigationSettingsTopbar,
  'portal-general-settings-navigation-settings-topbar-preview': NavigationSettingsTopbarPreview,
  'portal-general-settings-navigation-settings-application-drawer': NavigationSettingsTopbarApplicationDrawer,
  'portal-general-settings-navigation-settings-sidebar': NavigationSettingsSidebar,
  'portal-general-settings-navigation-settings-sidebar-preview': NavigationSettingsSidebarPreview,
  'portal-general-settings-navigation-settings-sidebar-preview-item': NavigationSettingsSidebarPreviewItem,
  'portal-general-settings-navigation-settings-sidebar-add-button': NavigationSettingsSidebarAddButton,
  'portal-general-settings-navigation-settings-sidebar-add-link-drawer': NavigationSettingsAddSidebarLinkDrawer,
  'portal-general-settings-navigation-settings-sidebar-add-site-drawer': NavigationSettingsAddSidebarSiteDrawer,
  'portal-general-settings-navigation-settings-sidebar-add-spaces-drawer': NavigationSettingsAddSidebarSpacesDrawer,
  'portal-general-settings-navigation-settings-icon-input': NavigationSettingsIconInput,
  'portal-general-settings-navigation-settings-icon': NavigationSettingsIcon,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
