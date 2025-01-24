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
      {{ $t('generalSettings.drawerStyling.drawer.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-card class="pa-4" flat>
        <p>
          {{ $t('generalSettings.drawerStyling.help1') }}
        </p>
        <p>
          {{ $t('generalSettings.drawerStyling.help2') }}
        </p>
        <div class="mt-2 pe-4 d-flex">
          <span class="text-title">
            {{ $t('generalSettings.background') }}
          </span>
        </div>
        <portal-general-settings-background-input
          v-if="initialized"
          v-model="backgroundProperties"
          class="mt-2 pe-3" />
        <div class="mt-2 pe-4 d-flex">
          <span class="text-title">
            {{ $t('generalSettings.text.label') }}
          </span>
        </div>
        <portal-general-settings-branding-text-input v-model="drawerTextProperties" />
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
          @click="updateDrawerStylingProperties">
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
    backgroundProperties: null,
    drawerTextProperties: null,
    defaultDrawerStylingProperties: null,
    initialized: false,
  }),
  props: {
    drawerStylingProperties: {
      type: Object,
      required: true
    },
  },
  computed: {
    saveButtonDisabled() {
      if (!this.backgroundProperties && !this.drawerTextProperties) {
        return false;
      }
      const oldDrawerProperties = Object.assign(JSON.parse(JSON.stringify(this.defaultDrawerStylingProperties)));
      const newDrawerProperties = {
        backgroundProperties: Object.assign(JSON.parse(JSON.stringify(this.backgroundProperties))),
        drawerTextProperties: Object.assign(JSON.parse(JSON.stringify(this.drawerTextProperties))),
      };
      return JSON.stringify(oldDrawerProperties) === JSON.stringify(newDrawerProperties);
    },
  },
  created() {
    this.$root.$on('open-drawer-styling', this.open);
  },
  beforeDestroy() {
    this.$root.$off('open-drawer-styling', this.open);
  },
  methods: {
    init() {
      this.backgroundProperties = {
        backgroundColor: this.drawerStylingProperties?.drawerBackgroundColor || null,
        backgroundPosition: this.drawerStylingProperties?.drawerBackgroundPosition || null,
        background: this.drawerStylingProperties?.drawerBackground || null,
        backgroundRepeat: this.drawerStylingProperties?.drawerBackgroundRepeat || null,
        backgroundSize: this.drawerStylingProperties?.drawerBackgroundSize || null,
        backgroundEffect: this.getDrawerBackgroundEffect()
      };
      this.drawerTextProperties = {
        textColor: this.drawerStylingProperties?.drawerTextColor,
        textFontSize: this.drawerStylingProperties?.drawerTextFontSize,
        textFontStyle: this.drawerStylingProperties?.drawerTextFontStyle,
        textFontWeight: this.drawerStylingProperties?.drawerTextFontWeight,
        textSubtitleColor: this.drawerStylingProperties?.drawerTextSubtitleColor,
        textSubtitleFontSize: this.drawerStylingProperties?.drawerTextSubtitleFontSize,
        textSubtitleFontStyle: this.drawerStylingProperties?.drawerTextSubtitleFontStyle,
        textSubtitleFontWeight: this.drawerStylingProperties?.drawerTextSubtitleFontWeight,
        textTitleColor: this.drawerStylingProperties?.drawerTextTitleColor,
        textTitleFontSize: this.drawerStylingProperties?.drawerTextTitleFontSize,
        textTitleFontStyle: this.drawerStylingProperties?.drawerTextTitleFontStyle,
        textTitleFontWeight: this.drawerStylingProperties?.drawerTextTitleFontWeight,
        textHeaderColor: this.drawerStylingProperties?.drawerTextHeaderColor,
        textHeaderFontSize: this.drawerStylingProperties?.drawerTextHeaderFontSize,
        textHeaderFontStyle: this.drawerStylingProperties?.drawerTextHeaderFontStyle,
        textHeaderFontWeight: this.drawerStylingProperties?.drawerTextHeaderFontWeight
      };
      this.defaultDrawerStylingProperties = {
        backgroundProperties: Object.assign(JSON.parse(JSON.stringify(this.backgroundProperties))),
        drawerTextProperties: Object.assign(JSON.parse(JSON.stringify(this.drawerTextProperties))),
      };
      this.initialized = true;
    },
    reset() {
      this.backgroundProperties = null;
      this.drawerTextProperties = null;
      this.defaultDrawerStylingProperties = null;
      this.initialized = false;
    },
    open() {
      this.init();
      this.$refs.drawer.open();
    },
    close() {
      this.reset();
      this.$refs.drawer.close();
    },
    getDrawerBackgroundEffect() {
      const effect = this.drawerStylingProperties?.drawerBackgroundImage;
      if (!effect || effect === 'none') {
        return null;
      }
      if (effect.includes('url')) {
        return effect.split('), ')[1];
      }
      return effect;
    },
    updateDrawerStylingProperties() {
      this.$root.$emit('update-drawer-styling-properties', this.backgroundProperties, this.drawerTextProperties);
      this.close();
    }
  }
};
</script>
