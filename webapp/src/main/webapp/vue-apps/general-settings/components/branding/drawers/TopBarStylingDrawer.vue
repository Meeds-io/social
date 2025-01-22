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
      {{ $t('generalSettings.topBarStyling.drawer.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-card class="pa-4" flat>
        <p>
          {{ $t('generalSettings.topBarStyling.help1') }}
        </p>
        <p>
          {{ $t('generalSettings.topBarStyling.help2') }} <a href="/portal/administration#navigation" target="_blank">{{ $t('generalSettings.topBarStyling.help.link') }}</a>
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
          v-model="topBarTextProperties"
          :custom-header="false"
          :custom-text="true"
          :custom-sub-title="false"
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
    backgroundProperties: null,
    topBarTextProperties: null,
    defaultTopBarStylingProperties: null,
    initialized: false,
  }),
  props: {
    topBarStylingProperties: {
      type: Object,
      required: true
    },
  },
  computed: {
    saveButtonDisabled() {
      if (!this.backgroundProperties && !this.topBarTextProperties) {
        return false;
      }
      const oldTopBarProperties = Object.assign(JSON.parse(JSON.stringify(this.defaultTopBarStylingProperties)));
      const newTopBarProperties = {
        backgroundProperties: Object.assign(JSON.parse(JSON.stringify(this.backgroundProperties))),
        topBarTextProperties: Object.assign(JSON.parse(JSON.stringify(this.topBarTextProperties))),
      };
      return JSON.stringify(oldTopBarProperties) === JSON.stringify(newTopBarProperties);
    },
  },
  created() {
    this.$root.$on('open-top-bar-styling-drawer', this.open);
  },
  beforeDestroy() {
    this.$root.$off('open-top-bar-styling-drawer', this.open);
  },
  methods: {
    init() {
      this.backgroundProperties = {
        backgroundColor: this.topBarStylingProperties?.topBarBackgroundColor || null,
        backgroundPosition: this.topBarStylingProperties?.topBarBackgroundPosition || null,
        background: this.topBarStylingProperties?.topBarBackground || null,
        backgroundRepeat: this.topBarStylingProperties?.topBarBackgroundRepeat || null,
        backgroundSize: this.topBarStylingProperties?.topBarBackgroundSize || null,
        backgroundEffect: this.getTopBarBackgroundEffect()
      };
      this.topBarTextProperties = {
        textColor: this.topBarStylingProperties?.topBarTextColor,
        textFontSize: this.topBarStylingProperties?.topBarTextFontSize,
        textFontStyle: this.topBarStylingProperties?.topBarTextFontStyle,
        textFontWeight: this.topBarStylingProperties?.topBarTextFontWeight
      };
      this.defaultTopBarStylingProperties = {
        backgroundProperties: Object.assign(JSON.parse(JSON.stringify(this.backgroundProperties))),
        topBarTextProperties: Object.assign(JSON.parse(JSON.stringify(this.topBarTextProperties))),
      };
      this.initialized = true;
    },
    reset() {
      this.backgroundProperties = null;
      this.topBarTextProperties = null;
      this.defaultTopBarStylingProperties = null;
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
    getTopBarBackgroundEffect() {
      const effect = this.topBarStylingProperties?.topBarBackgroundImage;
      if (!effect || effect === 'none') {
        return null;
      }
      if (effect.includes('url')) {
        return effect.split('), ')[1];
      }
      return effect;
    },
    updatePageStylingProperties() {
      this.$root.$emit('update-top-bar-styling-properties', this.backgroundProperties, this.topBarTextProperties);
      this.close();
    }
  }
};
</script>
