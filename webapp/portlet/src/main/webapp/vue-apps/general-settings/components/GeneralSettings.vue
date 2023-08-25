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
        class="px-6"
        flat>
        <v-list-item dense class="px-0 mb-4">
          <v-list-item-action v-if="$root.selectedTab" class="my-auto me-0 ms-n2">
            <v-btn
              size="24"
              icon
              @click="$root.selectedTab = null">
              <v-icon size="18" class="icon-default-color">
                {{ $vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left' }}
              </v-icon>
            </v-btn>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title>
              <h4 class="font-weight-bold">{{ $t('generalSettings.title') }}</h4>
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>

        <v-expand-transition>
          <portal-general-settings-branding-site
            v-if="$root.selectedTab === 'branding'"
            ref="brandingSettings"
            :branding="branding"
            @saved="init"
            @close="$root.selectedTab = null" />
          <portal-general-settings-branding-login
            v-else-if="$root.selectedTab === 'login'"
            ref="loginSettings"
            :branding="branding"
            @saved="init"
            @close="$root.selectedTab = null" />
          <portal-general-settings-hub-access
            v-else-if="$root.selectedTab === 'access'"
            ref="loginSettings"
            :branding="branding"
            @saved="init"
            @close="$root.selectedTab = null" />
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
                  <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
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
                  <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
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
                  <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
                </v-btn>
              </v-list-item-action>
            </v-list-item>
          </div>
        </v-expand-transition>
      </v-card>
    </v-main>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    branding: null,
    errorMessage: null,
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
  mounted() {
    this.init()
      .finally(() => this.$root.$applicationLoaded());
  },
  methods: {
    init() {
      this.$root.loading = true;
      return this.$brandingService.getBrandingInformation()
        .then(data => this.branding = data)
        .finally(() => this.$root.loading = false);
    },
  },
};
</script>