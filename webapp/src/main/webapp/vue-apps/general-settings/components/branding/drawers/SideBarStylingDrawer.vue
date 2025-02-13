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
      {{ $t('generalSettings.sideBarStyling.drawer.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-card class="pa-4" flat>
        <p>
          {{ $t('generalSettings.sideBarStyling.help1') }}
        </p>
        <p>
          {{ $t('generalSettings.sideBarStyling.help2') }} <a href="/portal/administration#navigation" target="_blank">{{ $t('generalSettings.help.link') }}</a>
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
        <portal-general-settings-branding-text-input
          v-model="sideBarTextProperties"
          :custom-header="false"
          :custom-text="true"
          :custom-sub-title="true"
          :custom-title="false" />
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
          @click="updateSideBarStylingProperties">
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
    sideBarTextProperties: null,
    defaultSideBarStylingProperties: null,
    initialized: false,
  }),
  props: {
    sideBarStylingProperties: {
      type: Object,
      required: true
    },
  },
  computed: {
    saveButtonDisabled() {
      if (!this.backgroundProperties && !this.sideBarTextProperties) {
        return false;
      }
      const oldSideBarProperties = Object.assign(JSON.parse(JSON.stringify(this.defaultSideBarStylingProperties)));
      const newSideBarProperties = {
        backgroundProperties: Object.assign(JSON.parse(JSON.stringify(this.backgroundProperties))),
        sideBarTextProperties: Object.assign(JSON.parse(JSON.stringify(this.sideBarTextProperties))),
      };
      return JSON.stringify(oldSideBarProperties) === JSON.stringify(newSideBarProperties);
    },
  },
  created() {
    this.$root.$on('open-sidebar-styling-drawer', this.open);
  },
  beforeDestroy() {
    this.$root.$off('open-sidebar-styling-drawer', this.open);
  },
  methods: {
    init() {
      this.backgroundProperties = {
        backgroundColor: this.sideBarStylingProperties?.sideBarBackgroundColor || null,
        backgroundPosition: this.sideBarStylingProperties?.sideBarBackgroundPosition || null,
        background: this.sideBarStylingProperties?.sideBarBackground || null,
        backgroundRepeat: this.sideBarStylingProperties?.sideBarBackgroundRepeat || null,
        backgroundSize: this.sideBarStylingProperties?.sideBarBackgroundSize || null,
        backgroundEffect: this.getSideBarBackgroundEffect()
      };
      this.sideBarTextProperties = {
        textColor: this.sideBarStylingProperties?.sideBarTextColor,
        textFontSize: this.sideBarStylingProperties?.sideBarTextFontSize,
        textFontStyle: this.sideBarStylingProperties?.sideBarTextFontStyle,
        textFontWeight: this.sideBarStylingProperties?.sideBarTextFontWeight,
        textSubtitleColor: this.sideBarStylingProperties?.sideBarTextSubtitleColor,
        textSubtitleFontSize: this.sideBarStylingProperties?.sideBarTextSubtitleFontSize,
        textSubtitleFontStyle: this.sideBarStylingProperties?.sideBarTextSubtitleFontStyle,
        textSubtitleFontWeight: this.sideBarStylingProperties?.sideBarTextSubtitleFontWeight
      };
      this.defaultSideBarStylingProperties = {
        backgroundProperties: Object.assign(JSON.parse(JSON.stringify(this.backgroundProperties))),
        sideBarTextProperties: Object.assign(JSON.parse(JSON.stringify(this.sideBarTextProperties))),
      };
      this.initialized = true;
    },
    reset() {
      this.backgroundProperties = null;
      this.sideBarTextProperties = null;
      this.defaultSideBarStylingProperties = null;
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
    getSideBarBackgroundEffect() {
      const effect = this.sideBarStylingProperties?.sideBarBackgroundImage;
      if (!effect || effect === 'none') {
        return null;
      }
      if (effect.includes('url')) {
        return effect.split('), ')[1];
      }
      return effect;
    },
    updateSideBarStylingProperties() {
      this.$root.$emit('update-sidebar-styling-properties', this.backgroundProperties, this.sideBarTextProperties);
      this.close();
    }
  }
};
</script>
