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
import './initComponents.js';
import './services.js';

// get overrided components if exists
const components = extensionRegistry.loadComponents('GeneralSettings');
if (components && components.length > 0) {
  components.forEach(cmp => {
    Vue.component(cmp.componentName, cmp.componentOptions);
  });
}

const appId = 'generalSettings';
const lang = window.eXo?.env?.portal?.language || 'en';

// Should expose the locale ressources as REST API 
const urls = [
  `/social-portlet/i18n/locale.portlet.Login?lang=${lang}`,
  `/social-portlet/i18n/locale.portal.login?lang=${lang}`,
  `/social-portlet/i18n/locale.portlet.Portlets?lang=${lang}`,
  `/social-portlet/i18n/locale.portlet.GeneralSettings?lang=${lang}`,
];

export function init(publicSiteVisible, publicSiteId) {
  exoi18n.loadLanguageAsync(lang, urls)
    .then(i18n =>
      Vue.createApp({
        data: {
          selectedTab: null,
          loading: false,
          publicSiteVisible,
          publicSiteId,
        },
        computed: {
          isMobile() {
            return this.$vuetify.breakpoint.mobile;
          },
        },
        watch: {
          loading() {
            if (this.loading) {
              document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
            } else {
              document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
            }
          },
          selectedTab() {
            if (this.selectedTab === 'access') {
              if (window.location.hash !== '#platformaccess') {
                window.location.hash = '#platformaccess';
              }
            } else if (this.selectedTab === 'branding') {
              if (window.location.hash !== '#display') {
                window.location.hash = '#display';
              }
            } else if (this.selectedTab === 'login') {
              if (window.location.hash !== '#logincustomization') {
                window.location.hash = '#logincustomization';
              }
            } else if (!this.selectedTab) {
              window.history.replaceState('', window.document.title, window.location.href.split('#')[0]);
            }
          },
        },
        template: `<portal-general-settings id="${appId}" />`,
        vuetify: Vue.prototype.vuetifyOptions,
        i18n
      }, `#${appId}`, 'General Settings')
    );
}
