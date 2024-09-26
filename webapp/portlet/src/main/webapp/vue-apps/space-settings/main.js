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
const urls = [
  `/social-portlet/i18n/locale.portlet.Portlets?lang=${lang}`,
  `/social-portlet/i18n/locale.portlet.social.PeopleListApplication?lang=${lang}`
];

const appId = 'SpaceSettings';

export function init(isExternalFeatureEnabled) {
  exoi18n.loadLanguageAsync(lang, urls)
    .then(i18n => 
      Vue.createApp({
        template: `<space-settings id="${appId}" />`,
        data: {
          isExternalFeatureEnabled,
          spaceId: eXo.env.portal.spaceId,
          space: null,
          activeSection: null,
          externalInvitations: null,
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
          document.addEventListener('hideSettingsApps', this.handleDisplaySectionEvent);
          document.addEventListener('showSettingsApps', this.handleShowMainEvent);

          this.$root.$on('space-settings-updated', this.handleSpaceUpdated);
          this.$root.$on('space-settings-managers-updated', this.handleSpaceUpdated);
          this.$root.$on('space-settings-publishers-updated', this.handleSpaceUpdated);
          this.$root.$on('space-settings-redactors-updated', this.handleSpaceUpdated);
          this.$root.$on('space-settings-members-updated', this.handlePendingUpdated);
          this.$root.$on('space-settings-pending-updated', this.handlePendingUpdated);
          this.init();
        },
        beforeDestroy() {
          document.removeEventListener('hideSettingsApps', this.handleDisplaySectionEvent);
          document.removeEventListener('showSettingsApps', this.handleShowMainEvent);

          this.$root.$off('space-settings-updated', this.handleSpaceUpdated);
          this.$root.$off('space-settings-managers-updated', this.handleSpaceUpdated);
          this.$root.$off('space-settings-publishers-updated', this.handleSpaceUpdated);
          this.$root.$off('space-settings-redactors-updated', this.handleSpaceUpdated);
          this.$root.$off('space-settings-members-updated', this.handlePendingUpdated);
          this.$root.$off('space-settings-pending-updated', this.handlePendingUpdated);
        },
        methods: {
          async init() {
            if (window.location.hash === '#overview') {
              this.$root.showSection('overview');
            } else if (window.location.hash === '#roles') {
              this.$root.showSection('roles');
            }
            await this.refreshSpace();
            await this.refreshExternalInvitations();
            document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
            this.$applicationLoaded();
          },
          async handlePendingUpdated() {
            await this.refreshSpace();
            await this.refreshExternalInvitations();
          },
          async handleSpaceUpdated() {
            const oldPrettyName = this.space.prettyName;
            await this.refreshSpace();
            if (oldPrettyName !== this.space.prettyName) {
              window.history.replaceState('', window.document.title, window.location.href.replaceAll(`/${oldPrettyName}/`, `/${this.space.prettyName}/`));
            }
            document.dispatchEvent(new CustomEvent('space-settings-updated', {detail: this.space}));
          },
          async refreshSpace() {
            if (this.spaceId) {
              this.space = await this.$spaceService.getSpaceById(this.spaceId, Date.now());
            }
          },
          async refreshExternalInvitations() {
            if (this.isExternalFeatureEnabled) {
              this.externalInvitations = await this.$spaceService.findSpaceExternalInvitationsBySpaceId(this.spaceId);
            }
          },
          showSection(sectionId) {
            document.dispatchEvent(new CustomEvent('hideSettingsApps', {detail: sectionId}));
          },
          showMain() {
            document.dispatchEvent(new CustomEvent('showSettingsApps'));
          },
          handleDisplaySectionEvent(event) {
            this.activeSection = event?.detail;
          },
          handleShowMainEvent() {
            this.activeSection = null;
          },
        },
        i18n,
        vuetify: Vue.prototype.vuetifyOptions,
      }, `#${appId}`, 'Space Settings')
    ).finally(() => Vue.prototype.$utils.includeExtensions('SpaceSettingExtension'));
}
