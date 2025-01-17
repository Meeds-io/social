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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    right>
    <template #title>
      {{ $t('generalSettings.pageAndAppsStyle.option.label') }}
    </template>
    <template v-if="drawer" #content>
      <v-card class="pa-4" flat>
        <p>
          {{ $t('generalSettings.pageAndAppsStyle.help1') }}
        </p>
        <p>
          {{ $t('generalSettings.pageAndAppsStyle.help2') }}
        </p>
        <div class="text-title mt-4">
          {{ $t('generalSettings.pageAndAppsStyle.drawer.page.design.title') }}
        </div>
        <div class="mt-2 pe-4 d-flex align-center">
          <span class="text-header">
            {{ $t('generalSettings.globalPageFullWindow') }}
          </span>
          <v-switch
              v-model="fullWindow"
              class="ms-auto my-auto me-n2" />
        </div>
        <portal-general-settings-background-input
            v-if="initialized"
            v-model="backgroundProperties"
            class="mt-2 pe-3"
            @change="setChangeStatus"
            @initialized="$emit('initialized')">
          <template #title>
            <span class="text-header">
              {{ $t('generalSettings.globalPageBackground') }}
            </span>
          </template>
        </portal-general-settings-background-input>
        <div class="text-title mt-4">
          {{ $t('generalSettings.pageAndAppsStyle.drawer.apps.styling.title') }}
        </div>
      </v-card>
    </template>
    <template #footer>
      <div class="d-flex justify-end">
        <v-btn
          class="btn ms-2"
          @click="close">
          {{ $t('generalSettings.button.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary ms-2"
          :disabled="saveButtonDisabled"
          @click="updatePageStylingProperties">
          {{ $t('generalSettings.button.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    fullWindow: false,
    backgroundProperties: null,
    initialized: false,
    changed: false
  }),
  props: {
    pageStylingProperties: {
      type: Object,
      required: true
    },
  },
  computed: {
    saveButtonDisabled() {
      return !this.changed;
    },
  },
  created() {
    this.$root.$on('open-update-page-style-drawer', this.open);
  },
  beforeDestroy() {
    this.$root.$off('open-update-page-style-drawer', this.open);
  },
  methods: {
    init() {
      this.backgroundProperties = this.toBackgroundProperties(this.pageStylingProperties.backgroundProperties);
      this.fullWindow = this.pageStylingProperties?.fullWindow;
      this.initialized = true;
    },
    reset() {
      this.backgroundProperties = null;
      this.fullWindow = null;
      this.initialized = false;
      this.changed = false;
    },
    open() {
      this.init();
      this.$refs.drawer.open();
    },
    close() {
      this.reset();
      this.$refs.drawer.close();
    },
    toBackgroundProperties(inputProperties) {
      return Object.fromEntries(
        Object.entries(inputProperties).map(([propertyKey, propertyValue]) => {
          const newKey = propertyKey.startsWith('pageBackground')
            ? propertyKey.replace('pageBackground', 'background')
            : propertyKey;
          return [newKey, propertyValue];
        })
      );
    },
    setChangeStatus() {
      this.changed = true;
    },
    updatePageStylingProperties() {
      this.$root.$emit('update-page-styling-properties', this.backgroundProperties, this.fullWindow);
      this.close();
    }
  }
};
</script>
