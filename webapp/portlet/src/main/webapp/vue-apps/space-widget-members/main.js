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
import './extensions.js';

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpaceMembers');
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
  `/social-portlet/i18n/locale.portlet.social.PeopleListApplication?lang=${lang}`,
  `/social-portlet/i18n/locale.portlet.social.SpaceInfosPortlet?lang=${lang}`
];

const appId = 'SpaceMembersApplication';

export function init(spaceId, isManager, isMember, isExternalFeatureEnabled, members) {
  if (!isManager && !members.length) {
    Vue.prototype.$updateApplicationVisibility(false, document.querySelector(`#${appId}`));
    return;
  }
  exoi18n.loadLanguageAsync(lang, urls)
    .then(i18n => {
      Vue.createApp({
        data: {
          isManager,
          isMember,
          members,
          isExternalFeatureEnabled,
          spaceId,
          space: null,
          externalInvitations: null,
        },
        computed: {
          isMobile() {
            return this.$vuetify.breakpoint.mobile;
          },
        },
        created() {
          this.init();
          this.$root.$on('space-settings-updated', this.handleSpaceUpdated);
          this.$root.$on('space-settings-members-updated', this.handleSpaceUpdated);
          this.$root.$on('space-settings-pending-updated', this.handlePendingUpdated);
        },
        mounted() {
          document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        },
        beforeDestroy() {
          this.$root.$off('space-settings-updated', this.handleSpaceUpdated);
          this.$root.$off('space-settings-members-updated', this.handleSpaceUpdated);
          this.$root.$off('space-settings-pending-updated', this.handlePendingUpdated);
        },
        methods: {
          async init() {
            if (eXo.env.portal.userName?.length) {
              await this.refreshSpace();
            }
            await this.refreshExternalInvitations();
            document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
          },
          handlePendingUpdated() {
            this.refreshSpace();
            this.refreshExternalInvitations();
          },
          async handleSpaceUpdated() {
            await this.refreshSpace();
            document.dispatchEvent(new CustomEvent('space-settings-updated', {detail: this.space}));
          },
          async refreshSpace() {
            if (this.spaceId) {
              this.space = await this.$spaceService.getSpaceById(this.spaceId, Date.now(), 'membersCount');
            }
          },
          async refreshExternalInvitations() {
            if (this.isExternalFeatureEnabled && this.isManager) {
              this.externalInvitations = await this.$spaceService.findSpaceExternalInvitationsBySpaceId(this.spaceId);
            }
          },
        },
        template: `<space-members-application id="${appId}" />`,
        i18n,
        vuetify: Vue.prototype.vuetifyOptions,
      }, `#${appId}`, 'Space Members');
    });
}
