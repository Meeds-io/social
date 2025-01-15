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
      {{ $t('generalSettings.themeColors.option.label') }}
    </template>
    <template v-if="drawer" #content>
      <v-card class="pa-4" flat>
        <p>
          {{ $t('generalSettings.themeColors.help1') }}
        </p>
        <p class="ma-0">
          {{ $t('generalSettings.themeColors.help2') }}
        </p>
        <p class="ma-0">
          {{ $t('generalSettings.themeColors.help3') }}
        </p>
        <p>
          {{ $t('generalSettings.themeColors.help4') }}
        </p>
        <div class="text-header mt-4">
          {{ $t('generalSettings.themeColors.drawer.content.header.title') }}
        </div>
        <div class="d-flex flex-column justify-space-between mt-4">
          <div class="pt-2">
            <portal-general-settings-color-picker
              v-model="primaryColor"
              :label="$t('generalSettings.primaryColor.label')" />
          </div>
          <div class="pt-2">
            <portal-general-settings-color-picker
              v-model="secondaryColor"
              :label="$t('generalSettings.secondaryColor.label')" />
          </div>
          <div class="pt-2">
            <portal-general-settings-color-picker
              v-model="tertiaryColor"
              :label="$t('generalSettings.tertiaryColor.label')" />
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
          @click="updateBrandingThemeColors">
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
    primaryColor: null,
    secondaryColor: null,
    tertiaryColor: null,
  }),
  props: {
    themeColors: {
      type: Object,
      required: true
    }
  },
  computed: {
    saveButtonDisabled() {
      return this.primaryColor === this.themeColors.primaryColor &&
          this.secondaryColor === this.themeColors.secondaryColor &&
          this.tertiaryColor === this.themeColors.tertiaryColor;
    }
  },
  created() {
    this.$root.$on('open-update-theme-colors-drawer', this.open);
  },
  beforeDestroy() {
    this.$root.$off('open-update-theme-colors-drawer', this.open);
  },
  methods: {
    init() {
      this.primaryColor = this.themeColors?.primaryColor;
      this.secondaryColor = this.themeColors?.secondaryColor;
      this.tertiaryColor = this.themeColors?.tertiaryColor;
    },
    reset() {
      this.primaryColor = null;
      this.secondaryColor = null;
      this.tertiaryColor = null;
    },
    open() {
      this.init();
      this.$refs.drawer.open();
    },
    close() {
      this.reset();
      this.$refs.drawer.close();
    },
    updateBrandingThemeColors() {
      this.$root.$emit('update-branding-theme-colors', this.primaryColor, this.secondaryColor, this.tertiaryColor);
      this.close();
    }
  }
};
</script>
