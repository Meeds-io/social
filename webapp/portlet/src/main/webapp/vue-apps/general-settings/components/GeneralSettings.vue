<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
    <v-main>
      <v-card
        min-height="calc(100vh - 160px)"
        class="px-6 card-border-radius overflow-hidden"
        flat>
        <template v-if="intialized">
          <v-expand-transition>
            <v-list-item
              v-if="$root.selectedTab"
              dense
              class="px-0 mb-4">
              <v-list-item-action class="my-auto me-0 ms-n2">
                <v-btn
                  :title="$t('generalSettings.access.backToMain')"
                  size="24"
                  icon
                  @click="close">
                  <v-icon size="18" class="icon-default-color">
                    {{ $vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left' }}
                  </v-icon>
                </v-btn>
              </v-list-item-action>
              <v-list-item-content>
                <v-list-item-title class="d-flex">
                  <v-card
                    :title="$t('generalSettings.access.backToMain')"
                    class="flex-grow-0 py-1"
                    flat
                    @click="close()">
                    <h4 class="font-weight-bold">{{ $t('generalSettings.title') }}</h4>
                  </v-card>
                </v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-expand-transition>
          <v-expand-transition>
            <portal-general-settings-branding-site-window
              v-if="$root.selectedTab === 'branding'"
              ref="brandingSettings"
              :branding="branding"
              @saved="init"
              @changed="changed = $event"
              @close="close" />
            <portal-general-settings-branding-login
              v-else-if="$root.selectedTab === 'login'"
              ref="loginSettings"
              :branding="branding"
              @saved="init"
              @changed="changed = $event"
              @close="close" />
            <portal-general-settings-hub-access
              v-else-if="$root.selectedTab === 'access'"
              ref="loginSettings"
              :registration-settings="registrationSettings"
              @saved="init"
              @changed="changed = $event"
              @close="close" />
            <div v-else>
              <v-list-item class="px-0" two-line>
                <v-list-item-content>
                  <v-list-item-title>
                    <h4 class="my-0">{{ $t('generalSettings.displayCharacteristics') }}</h4>
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    {{ $t('generalSettings.subtitle.displayCharacteristics') }}
                  </v-list-item-subtitle>
                </v-list-item-content>
                <v-list-item-action>
                  <v-btn
                    icon
                    @click="$root.selectedTab = 'branding'">
                    <v-icon size="18" class="icon-default-color">fa-caret-right</v-icon>
                  </v-btn>
                </v-list-item-action>
              </v-list-item>
              <v-list-item class="px-0" two-line>
                <v-list-item-content>
                  <v-list-item-title>
                    <h4 class="my-0">{{ $t('generalSettings.loginCharacteristics') }}</h4>
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    {{ $t('generalSettings.subtitle.loginCharacteristics') }}
                  </v-list-item-subtitle>
                </v-list-item-content>
                <v-list-item-action>
                  <v-btn
                    icon
                    @click="$root.selectedTab = 'login'">
                    <v-icon size="18" class="icon-default-color">fa-caret-right</v-icon>
                  </v-btn>
                </v-list-item-action>
              </v-list-item>
              <v-list-item class="px-0" two-line>
                <v-list-item-content>
                  <v-list-item-title>
                    <h4 class="my-0">{{ $t('generalSettings.access') }}</h4>
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    {{ $t('generalSettings.subtitle.access') }}
                  </v-list-item-subtitle>
                </v-list-item-content>
                <v-list-item-action>
                  <v-btn
                    icon
                    @click="$root.selectedTab = 'access'">
                    <v-icon size="18" class="icon-default-color">fa-caret-right</v-icon>
                  </v-btn>
                </v-list-item-action>
              </v-list-item>
              <v-list-item class="px-0" two-line>
                <v-list-item-content>
                  <v-list-item-title>
                    <h4 class="my-0">{{ $t('generalSettings.managePublicSite') }}</h4>
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    {{ $t('generalSettings.subtitle.managePublicSite') }}
                  </v-list-item-subtitle>
                </v-list-item-content>
                <v-list-item-action>
                  <v-btn
                    icon
                    @click="$root.$emit('public-site-edit')">
                    <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
                  </v-btn>
                </v-list-item-action>
              </v-list-item>
            </div>
          </v-expand-transition>
        </template>
      </v-card>
      <exo-confirm-dialog
        ref="closeConfirmDialog"
        :title="$t('generalSettings.closeTabConfirmTitle')"
        :message="$t('generalSettings.closeTabConfirmMessage')"
        :ok-label="$t('generalSettings.yes')"
        :cancel-label="$t('generalSettings.no')"
        persistent
        @ok="closeEffectively" />
      <portal-general-settings-public-site-drawer />
    </v-main>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    branding: null,
    registrationSettings: null,
    errorMessage: null,
    intialized: false,
    changed: false,
  }),
  watch: {
    errorMessage() {
      if (this.errorMessage) {
        this.$root.$emit('alert-message', this.$t(this.errorMessage), 'error');
      } else {
        this.$root.$emit('close-alert-message');
      }
    },
  },
  created() {
    if (window.location.hash === '#platformaccess') {
      this.$root.selectedTab = 'access';
    } else if (window.location.hash === '#display') {
      this.$root.selectedTab = 'branding';
    } else if (window.location.hash === '#logincustomization') {
      this.$root.selectedTab = 'login';
    }
  },
  mounted() {
    this.init()
      .then(() => this.$nextTick())
      .finally(() => {
        this.$root.$applicationLoaded();
        this.intialized = true;
      });
  },
  methods: {
    init() {
      this.$root.loading = true;
      return this.$brandingService.getBrandingInformation()
        .then(data => this.branding = data)
        .then(() => this.$registrationService.getRegistrationSettings())
        .then(data => this.registrationSettings = data)
        .finally(() => this.$root.loading = false);
    },
    close() {
      if (this.changed) {
        this.$refs.closeConfirmDialog.open();
      } else {
        this.closeEffectively();
      }
    },
    closeEffectively() {
      this.confirmClose = false;
      this.$nextTick().then(() => {
        this.$root.selectedTab = null;
        this.changed = false;
      });
    },
  },
};
</script>
