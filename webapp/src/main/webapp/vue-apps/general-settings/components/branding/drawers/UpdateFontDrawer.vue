<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

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
      {{ $t('generalSettings.fontFamily.drawer.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-card class="pa-4" flat>
        <p>
          {{ $t('generalSettings.fontFamily.help1') }}
        </p>
        <div class="text-header mt-4">
          {{ $t('generalSettings.fontFamily.select.label') }}
        </div>
        <v-radio-group
          v-model="selectedFontFamily"
          class="mt-2">
          <v-radio
            v-for="family in fontFamilies"
            :key="family"
            :label="family"
            :value="family"
            :style="{ fontFamily: family }" />
        </v-radio-group>
        <div class="text-header mt-4">
          {{ $t('generalSettings.fontFamily.preview.label') }}
        </div>
        <div
          class="d-flex flex-column mt-2 pa-4 border-color border-radius"
          :style="{ fontFamily: selectedFontFamily }">
          <div class="text-title">
            {{ $t('generalSettings.fontFamily.preview.title') }}
          </div>
          <div class="text-header mt-2">
            {{ $t('generalSettings.fontFamily.preview.header') }}
          </div>
          <div class="text-body mt-2">
            {{ $t('generalSettings.fontFamily.preview.body') }}
          </div>
          <div class="text-subtitle mt-2">
            {{ $t('generalSettings.fontFamily.preview.subtitle') }}
          </div>
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
          @click="updateBrandingFontFamily">
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
    selectedFontFamily: null,
  }),
  props: {
    fontFamily: {
      type: String,
      default: null,
    },
    fontFamilies: {
      type: Array,
      default: () => [],
    },
  },
  computed: {
    saveButtonDisabled() {
      return !this.selectedFontFamily || this.selectedFontFamily === this.fontFamily;
    },
  },
  created() {
    this.$root.$on('open-update-font-drawer', this.open);
  },
  beforeDestroy() {
    this.$root.$off('open-update-font-drawer', this.open);
  },
  methods: {
    open() {
      this.selectedFontFamily = this.fontFamily;
      this.$refs.drawer.open();
    },
    close() {
      this.selectedFontFamily = null;
      this.$refs.drawer.close();
    },
    updateBrandingFontFamily() {
      this.$root.$emit('update-branding-font-family', this.selectedFontFamily);
      this.close();
    },
  },
};
</script>
