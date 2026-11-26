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
        <portal-general-settings-theme-version-bar @change="changeVersion" />
        <p>
          {{ $t('generalSettings.themeColors.help5') }}
        </p>
        <div class="text-header mt-4">
          {{ $t('generalSettings.themeColors.drawer.content.header.title') }}
        </div>
        <v-list-item
          class="themeColorRow themeColorRow-primaryColor pa-0 mt-4"
          dense>
          <v-list-item-content class="my-auto">
            <v-list-item-title class="text-body">
              {{ $t('generalSettings.primaryColor.label') }}
            </v-list-item-title>
            <v-list-item-subtitle class="text-subtitle text-wrap">
              {{ $t('generalSettings.themeColors.primary.help') }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action class="my-auto ms-2">
            <styling-color-picker
              v-model="primaryColor"
              :aria-label="$t('generalSettings.primaryColor.label')"
              :restore-label="restoreLabel('primaryColor', primaryColor)"
              min-text-width="78"
              @restore="restoreColor('primaryColor')" />
          </v-list-item-action>
        </v-list-item>
        <v-list-item
          class="themeColorRow themeColorRow-secondaryColor pa-0 mt-2"
          dense>
          <v-list-item-content class="my-auto">
            <v-list-item-title class="text-body">
              {{ $t('generalSettings.secondaryColor.label') }}
            </v-list-item-title>
            <v-list-item-subtitle class="text-subtitle text-wrap">
              {{ $t('generalSettings.themeColors.secondary.help') }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action class="my-auto ms-2">
            <styling-color-picker
              v-model="secondaryColor"
              :aria-label="$t('generalSettings.secondaryColor.label')"
              :restore-label="restoreLabel('secondaryColor', secondaryColor)"
              min-text-width="78"
              @restore="restoreColor('secondaryColor')" />
          </v-list-item-action>
        </v-list-item>
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
          :disabled="saveButtonDisabled && !draftChanged"
          @click="updateBrandingThemeColors">
          {{ $t('generalSettings.button.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
import ThemeAreaDrawerMixin from '../../../js/ThemeAreaDrawerMixin.js';

export default {
  mixins: [ThemeAreaDrawerMixin],
  data: () => ({
    drawer: false,
    primaryColor: null,
    secondaryColor: null,
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
          this.secondaryColor === this.themeColors.secondaryColor;
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
    },
    reset() {
      this.primaryColor = null;
      this.secondaryColor = null;
    },
    open() {
      this.init();
      this.$refs.drawer.open();
    },
    close() {
      this.reset();
      this.$refs.drawer.close();
    },
    commitEdits() {
      this.$root.$emit('update-branding-theme-colors', this.primaryColor, this.secondaryColor);
    },
    updateBrandingThemeColors() {
      this.saveAndClose();
    }
  }
};
</script>
