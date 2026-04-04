<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-app>
    <v-card
      class="application-body"
      flat>
      <v-card-title class="text-title pb-0">
        {{ $t('UserSettings.security.title') }}
      </v-card-title>
      <v-list>
        <v-list-item v-if="$root.isEmailEditable" dense>
          <v-list-item-content>
            <v-list-item-title>
              {{ $t('UserSettings.security.emailChange.title') }}
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action>
            <v-tooltip bottom>
              <template #activator="{on, attrs}">
                <div
                  v-on="on"
                  v-bind="attrs">
                  <v-btn
                    :aria-label="emailChangeTooltip"
                    small
                    icon
                    @click="$refs.emailDrawer.open()">
                    <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
                  </v-btn>
                </div>
              </template>
              <span>{{ emailChangeTooltip }}</span>
            </v-tooltip>  
          </v-list-item-action>
        </v-list-item>
        <v-list-item dense>
          <v-list-item-content>
            <v-list-item-title>
              {{ $t('UserSettings.security.passwordChange.title') }}
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action>
            <v-tooltip bottom>
              <template #activator="{on, attrs}">
                <div
                  v-on="on"
                  v-bind="attrs">
                  <v-btn
                    :aria-label="passwordChangeTooltip"
                    :disabled="!allowedToChangePassword"
                    small
                    icon
                    @click="$refs.passwordDrawer.open()">
                    <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
                  </v-btn>
                </div>
              </template>
              <span>{{ passwordChangeTooltip }}</span>
            </v-tooltip>  
          </v-list-item-action>
        </v-list-item>
        <extension-registry-components
          name="UserSettingsSecurity"
          type="user-settings-security"
          parent-element="div"
          element="div"
          class=" d-flex flex-column" />
      </v-list>
    </v-card>
    <user-setting-security-email-drawer
      ref="emailDrawer" />
    <user-setting-security-password-drawer
      ref="passwordDrawer" />
  </v-app>
</template>

<script>
export default {
  data: () => ({
    displayed: true,
    allowedToChangePassword: false,
  }),
  computed: {
    passwordChangeTooltip() {
      if (this.$root.ssoEnabled) {
        return this.$t('UserSettings.passwordChange.sso.enabled');
      } else {
        return this.allowedToChangePassword ? this.$t('UserSettings.button.tooltip.enabled') : this.$t('UserSettings.button.tooltip.disabled');
      }
    },
    emailChangeTooltip() {
      return this.$t('UserSettings.security.emailChange.tooltip');
    },
  },
  watch: {
    displayed() {
      if (this.displayed) {
        this.$nextTick().then(() => this.$root.$emit('application-cache'));
      }
      this.$root.$updateApplicationVisibility(this.displayed);
    },
  },
  created() {
    document.addEventListener('showSettingsApps', this.showApp);
    document.addEventListener('hideSettingsApps', this.hideApp);
    this.init();
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$applicationLoaded());
    this.$root.$updateApplicationVisibility(this.displayed);
  },
  methods: {
    async init() {
      const data = await this.$userService.isSynchronizedUserAllowedToChangePassword();
      this.allowedToChangePassword = !this.$root.ssoEnabled && data?.isSynchronizedUserAllowedToChangePassword === 'true';
    },
    showApp() {
      this.displayed = true;
    },
    hideApp() {
      this.displayed = false;
    },
  }
};
</script>

