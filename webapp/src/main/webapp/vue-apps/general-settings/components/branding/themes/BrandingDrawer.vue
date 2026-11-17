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
    id="brandingDrawer"
    ref="drawer"
    v-model="drawer"
    right>
    <template #title>
      {{ $t('generalSettings.branding.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-card class="pa-4" flat>
        <p>
          {{ $t('generalSettings.branding.drawer.description') }}
        </p>
        <div class="text-header mt-4 mb-2">
          {{ $t('generalSettings.companyNameTitle') }}
        </div>
        <v-text-field
          id="companyName"
          v-model="companyName"
          :placeholder="$t('generalSettings.companyNamePlaceholder')"
          class="setup-company-name border-box-sizing pt-0"
          name="companyName"
          type="text"
          aria-required="true"
          outlined
          dense />
        <div class="text-header mt-4">
          {{ $t('generalSettings.branding.logo.title') }}
        </div>
        <p class="text-subtitle">
          {{ $t('generalSettings.branding.logo.help') }}
        </p>
        <div class="d-flex justify-space-around">
          <portal-general-settings-branding-image-tile
            v-for="key in ['logo', 'darkLogo']"
            :id="`${key}Tile`"
            :key="key"
            :input-id="`${key}FileInput`"
            :label="tileLabel(key)"
            :help="key.startsWith('dark') && $t('generalSettings.branding.tile.darkHelp') || null"
            :src="tileSrc(key)"
            accept="image/png,image/jpeg,image/svg+xml"
            @upload="(uploadId, data) => upload(key, uploadId, data)"
            @remove="remove(key)" />
        </div>
        <div class="text-header mt-6">
          {{ $t('generalSettings.companyFaviconTitle') }}
        </div>
        <p class="text-subtitle">
          {{ $t('generalSettings.branding.favicon.help') }}
        </p>
        <div class="d-flex justify-space-around">
          <portal-general-settings-branding-image-tile
            v-for="key in ['favicon', 'darkFavicon']"
            :id="`${key}Tile`"
            :key="key"
            :input-id="`${key}FileInput`"
            :label="tileLabel(key)"
            :help="key.startsWith('dark') && $t('generalSettings.branding.tile.darkHelp') || null"
            :src="tileSrc(key)"
            :max-size="10240"
            size-error-key="generalSettings.favicon.tooBigFile.label"
            accept="image/png,image/svg+xml,image/x-icon,image/vnd.microsoft.icon"
            @upload="(uploadId, data) => upload(key, uploadId, data)"
            @remove="remove(key)" />
        </div>
      </v-card>
    </template>
    <template #footer>
      <div class="d-flex justify-end">
        <v-btn
          class="brandingDrawerCancelButton btn ms-2"
          @click="close">
          {{ $t('generalSettings.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="!companyName || !companyName.trim()"
          class="brandingDrawerApplyButton btn btn-primary ms-2"
          @click="apply">
          {{ $t('generalSettings.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  props: {
    // the page's working copy: its company name and the state of the four images
    value: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    drawer: false,
    companyName: null,
    identity: null,
  }),
  methods: {
    open() {
      this.companyName = this.value.companyName;
      this.identity = JSON.parse(JSON.stringify(this.value.identity));
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    apply() {
      this.$emit('apply', this.companyName.trim(), this.identity);
      this.close();
    },
    tileLabel(key) {
      return this.$t(key.startsWith('dark') && 'generalSettings.branding.tile.dark' || 'generalSettings.branding.tile.light');
    },
    // image shown in a tile: the unsaved upload, else the stored image (none once removed)
    tileSrc(key) {
      const file = this.identity[key];
      if (file.data) {
        return this.$utils.convertImageDataAsSrc(file.data);
      }
      if (file.remove || !file.fileId) {
        return null;
      }
      return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/platform/branding/${key}?v=${file.fileId}`;
    },
    upload(key, uploadId, data) {
      this.$set(this.identity, key, Object.assign({}, this.identity[key], {uploadId, data, remove: false}));
    },
    remove(key) {
      this.$set(this.identity, key, Object.assign({}, this.identity[key], {uploadId: null, data: null, remove: true}));
    },
  },
};
</script>
