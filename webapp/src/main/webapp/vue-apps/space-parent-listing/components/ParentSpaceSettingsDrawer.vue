<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
  <exo-drawer
    ref="drawer"
    :loading="loading"
    :right="!$vuetify.rtl">
    <template slot="title">
      <span class="text-color ma-auto">
        {{ $t('parentSpaceListing.settings.drawer.title') }}
      </span>
    </template>
    <template slot="content">
      <div class="d-flex flex-column pa-4">
        <translation-text-field
          ref="headerTitleInput"
          id="headerTitleInput"
          v-model="settings.headerTranslations"
          :placeholder="$t('parentSpaceListing.updateHeader.label')"
          no-expand-icon
          autofocus>
          <template #title>
            <span class="text-header">{{ $t('parentSpaceListing.updateHeader.label') }}</span>
          </template>
        </translation-text-field>
      </div>
    </template>
    <template
      slot="footer">
      <div class="d-flex justify-end">
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('parentSpaceListing.settings.drawer.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          :disabled="!enabled"
          @click="saveApplicationSettings">
          {{ $t('parentSpaceListing.settings.drawer.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data() {
    return {
      headerTitle: null,
      settings: {},
      originalSettings: {},
      loading: false
    };
  },
  computed: {
    enabled() {
      return JSON.stringify(this.settings) !== JSON.stringify(this.originalSettings) &&
          Object.values(this.settings?.headerTranslations).some(
            v => typeof v === 'string' && v.trim().length > 0
          );
    },
  },
  watch: {
    settings() {
      if (Object.keys(this.settings.headerTranslations).length === 0) {
        this.settings.headerTranslations = {[this.$root.defaultLanguage]: this.$t('parentSpaceListing.header.label')};
        this.originalSettings.headerTranslations = {[this.$root.defaultLanguage]: this.$t('parentSpaceListing.header.label')};
      }
    },
  },
  created() {
    this.$root.$on('parent-space-settings-drawer', this.open);
  },
  beforeDestroy() {
    this.$root.$off('parent-space-settings-drawer', this.open);
  },
  methods: {
    saveApplicationSettings() {
      this.loading = true;
      this.$parentSpaceListingService.saveSettings(this.$root.saveSettingsUrl , this.settings).then(() => {
        this.$root.settings = this.settings;
        this.close();
      }).finally(() => this.loading = false);
    },
    open() {
      this.settings = JSON.parse(JSON.stringify(this.$root.settings));
      this.originalSettings = JSON.parse(JSON.stringify(this.settings));
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    }
  }
};
</script>
