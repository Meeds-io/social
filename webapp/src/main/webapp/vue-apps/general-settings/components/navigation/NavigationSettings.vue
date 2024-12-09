<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
  <div v-if="initialized" class="mb-4">
    <portal-general-settings-navigation-settings-topbar
      :settings="navigationSettings"
      @changed="navigationSettings.topbar = $event" />
    <v-divider class="my-8" />
    <portal-general-settings-navigation-settings-sidebar
      :settings="navigationSettings"
      @changed="navigationSettings.sidebar = $event" />
    <div class="d-inline pt-8">
      <sticky-position-element
        scroll-diff="50"
        bottom="0">
        <div class="d-flex justify-end py-4 pe-5">
          <v-btn
            :aria-label="$t('generalSettings.cancel')"
            :disabled="loading"
            class="btn cancel-button me-4"
            elevation="0"
            @click="$emit('close')">
            <span class="text-none">
              {{ $t('generalSettings.cancel') }}
            </span>
          </v-btn>
          <v-btn
            :aria-label="$t('generalSettings.apply')"
            :disabled="!modified"
            :loading="loading"
            color="primary"
            class="btn btn-primary"
            elevation="0"
            @click="save">
            <span class="text-none">
              {{ $t('generalSettings.apply') }}
            </span>
          </v-btn>
        </div>
      </sticky-position-element>
    </div>
    <portal-general-settings-navigation-settings-sidebar-add-link-drawer />
    <portal-general-settings-navigation-settings-sidebar-add-site-drawer />
    <portal-general-settings-navigation-settings-sidebar-add-spaces-drawer />
  </div>
</template>
<script>
export default {
  data: () => ({
    navigationSettings: {
      topbar: {
        applications: [],
        displayCompanyName: true,
        displayMobileCompanyLogo: false,
        displaySiteName: true,
      },
      sidebar: {
        items: [],
        allowUserCustomHome: false,
        defaultMode: 'HIDDEN',
        allowedModes: ['HIDDEN','ICON','STICKY'],
      },
    },
    originalNavigationSettings: null,
    loading: false,
    initialized: false,
  }),
  computed: {
    modified() {
      return JSON.stringify(this.originalNavigationSettings) !== JSON.stringify(this.navigationSettings);
    },
  },
  async created() {
    await this.refresh();
    this.initialized = true;
  },
  methods: {
    async refresh() {
      this.loading = true;
      try {
        this.navigationSettings = await this.$navigationConfigurationService.getConfiguration();
        this.originalNavigationSettings = JSON.parse(JSON.stringify(this.navigationSettings));
      } finally {
        this.loading = false;
      }
    },
    save() {
      this.loading = true;
      try {
        this.$navigationConfigurationService.saveConfiguration(this.navigationSettings);
      } finally {
        this.$root.$emit('alert-message', this.$t('generalSettings.navigationSettingsUpdatedSuccessfully'), 'success');
        this.originalNavigationSettings = JSON.parse(JSON.stringify(this.navigationSettings));
        window.setTimeout(() => this.loading = false, 200);
      }
    },
  },
};
</script>
