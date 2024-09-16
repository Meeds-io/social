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

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpaceSettings');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

//getting language of user
const lang = eXo && eXo.env.portal.language || 'en';

//should expose the locale ressources as REST API
const url = `/social-portlet/i18n/locale.portlet.Portlets?lang=${lang}`;

const appId = 'SpaceSettings';

export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => 
    Vue.createApp({
      template: '<space-settings />',
      data: {
        spaceId: eXo.env.portal.spaceId,
        space: null,
        activeSection: null,
      },
      computed: {
        isOverviewSection() {
          return this.activeSection === 'overview';
        },
        isRolesSection() {
          return this.activeSection === 'roles';
        },
        isAllSections() {
          return !this.activeSection;
        },
        isMobile() {
          return this.$vuetify.breakpoint.mobile;
        },
      },
      watch: {
        activeSection() {
          if (this.$root.isAllSections) {
            window.history.replaceState('', window.document.title, window.location.href.split('#')[0]);
          } else {
            window.history.replaceState('', window.document.title, `${window.location.href.split('#')[0]}#${this.$root.activeSection}`);
          }
          this.$root.$emit('close-alert-message');
        },
      },
      created() {
        this.init();
        document.addEventListener('hideSettingsApps', this.showSection);
        document.addEventListener('showSettingsApps', this.showMain);
        this.$root.$on('space-settings-updated', this.handleSpaceUpdated);
      },
      beforeDestroy() {
        document.removeEventListener('hideSettingsApps', this.showSection);
        document.removeEventListener('showSettingsApps', this.showMain);
        this.$root.$off('space-settings-updated', this.handleSpaceUpdated);
      },
      methods: {
        async init() {
          if (window.location.hash === '#overview') {
            this.$root.activeSection = 'overview';
          } else if (window.location.hash === '#roles') {
            this.$root.activeSection = 'roles';
          }
          await this.refreshSpace();
          document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
          this.$applicationLoaded();
        },
        async handleSpaceUpdated() {
          const oldPrettyName = this.space.prettyName;
          await this.refreshSpace();
          if (oldPrettyName !== this.space.prettyName) {
            window.history.replaceState('', window.document.title, window.location.href.replaceAll(oldPrettyName, this.space.prettyName));
          }
          document.dispatchEvent(new CustomEvent('space-settings-updated', {detail: this.space}));
        },
        async refreshSpace() {
          if (this.spaceId) {
            this.space = await this.$spaceService.getSpaceById(this.spaceId, Date.now());
          }
        },
        showSection(event) {
          this.activeSection = event?.detail;
        },
        showMain() {
          this.activeSection = null;
        },
      },
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'Space Settings')
  ).finally(() => Vue.prototype.$utils.includeExtensions('SpaceSettingExtension'));
}
