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
      {{ $t('generalSettings.pageStyling.drawer.title') }}
    </template>
    <template v-if="drawer" #content>
      <v-card class="pa-4" flat>
        <p>
          {{ $t('generalSettings.pageStyling.help1') }}
        </p>
        <div class="mt-2 pe-4 d-flex flex-column">
          <span class="text-title">
            {{ $t('generalSettings.page.styling.label') }}
          </span>
          <span class="text-header mt-2">
            {{ $t('generalSettings.page.width.label') }}
          </span>
          <v-list-item
            class="pa-0"
            dense>
            <v-list-item-content class="my-auto">
              <v-radio-group
                v-model="choice"
                class="my-auto text-no-wrap flex-grow-1 flex-shrink-0"
                mandatory>
                <v-radio
                  value="custom"
                  class="mx-0">
                  <template #label>
                    <span>{{ $t('generalSettings.page.custom.width.label') }}</span>
                  </template>
                </v-radio>
                <v-radio
                  value="full"
                  class="mx-0">
                  <template #label>
                    <span>{{ $t('generalSettings.page.full.width.label') }}</span>
                  </template>
                </v-radio>
              </v-radio-group>
            </v-list-item-content>
            <v-list-item-action
              :class="choice === 'custom' && 'mb-auto' || 'my-auto'"
              class="me-0 ms-auto">
              <number-input
                v-if="choice === 'custom'"
                v-model="pageWidth"
                :min="100"
                :max="3000"
                :step="10"
                editable />
            </v-list-item-action>
          </v-list-item>
          <span class="text-header mt-2">
            {{ $t('generalSettings.page.background') }}
          </span>
          <portal-general-settings-background-input
            v-if="initialized"
            v-model="backgroundProperties"
            :default-background-color="defaultPageBackground"
            class="mt-2 pe-3" />
        </div>
        <div class="mt-2 pe-4 d-flex flex-column">
          <span class="text-title">
            {{ $t('generalSettings.application.styling.label') }}
          </span>
          <span class="text-header my-2">
            {{ $t('generalSettings.application.styling.radius.label') }}
          </span>
          <portal-general-settings-border-radius
            v-model="borderRadius"
            ref="borderRadius"
            @input="borderRadius = $event" />
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
    backgroundProperties: null,
    defaultCustomPageWidth: '1320',
    defaultPageBackground: '#F0F0F0FF',
    pageWidth: null,
    borderRadius: null,
    defaultPageStylingProperties: null,
    choice: null,
    initialized: false,

  }),
  props: {
    pageStylingProperties: {
      type: Object,
      required: true
    },
  },
  computed: {
    saveButtonDisabled() {
      if (!this.backgroundProperties || !this.pageWidth) {
        return false;
      }
      return JSON.stringify(this.backgroundProperties) === JSON.stringify(this.defaultPageStylingProperties.backgroundProperties) &&
          this.defaultPageStylingProperties.borderRadius === this.borderRadius && this.defaultPageStylingProperties.pageWidth === this.pageWidth;
    },
  },
  watch: {
    choice() {
      if (this.initialized) {
        if (this.choice === 'custom') {
          this.pageWidth = this.defaultCustomPageWidth;
        } else {
          this.pageWidth = '100%';
        }
      }
    }
  },
  created() {
    this.$root.$on('open-page-styling-drawer', this.open);
  },
  beforeDestroy() {
    this.$root.$off('open-page-styling-drawer', this.open);
  },
  methods: {
    init() {
      this.backgroundProperties = {
        backgroundColor: this.normalizeBackgroundColor(this.pageStylingProperties?.pageBackgroundColor) || this.defaultPageBackground,
        backgroundPosition: this.pageStylingProperties?.pageBackgroundPosition || null,
        background: this.pageStylingProperties?.pageBackground || null,
        backgroundRepeat: this.pageStylingProperties?.pageBackgroundRepeat || null,
        backgroundSize: this.pageStylingProperties?.pageBackgroundSize || null,
        backgroundEffect: this.getPageBackgroundEffect()
      };
      this.pageWidth = this.pageStylingProperties.pageWidth;
      this.borderRadius = this.pageStylingProperties.borderRadius;
      if (!this.pageWidth ) {
        this.pageWidth = this.defaultCustomPageWidth;
      }
      if (this.pageWidth.endsWith('px') || this.pageWidth === this.defaultCustomPageWidth) {
        this.choice = 'custom';
        this.pageWidth = Number(this.pageWidth.split('px')[0]);
      } else {
        this.choice = 'full';
        this.pageWidth = Number(this.pageWidth.split('%')[0]);
      }
      this.defaultPageStylingProperties = {
        backgroundProperties: Object.assign(JSON.parse(JSON.stringify(this.backgroundProperties))),
        pageWidth: this.pageWidth,
        borderRadius: this.borderRadius
      };
      this.initialized = true;
    },
    reset() {
      this.backgroundProperties = null;
      this.pageWidth = null;
      this.borderRadius = null;
      this.defaultPageStylingProperties = null;
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
    normalizeBackgroundColor(color) {
      if (!color) {
        return color;
      }
      return color.length === 7 ? `${color}FF` : color;
    },
    getPageBackgroundEffect() {
      const effect = this.pageStylingProperties?.pageBackgroundEffect;
      if (!effect || effect === 'none') {
        return null;
      }
      if (effect.includes('url')) {
        return effect.split('), ')[1];
      }
      return effect;
    },
    updatePageStylingProperties() {
      this.pageWidth = this.choice === 'custom' && `${this.pageWidth}px` || '100%';
      this.$root.$emit('update-page-styling-properties', this.backgroundProperties, this.borderRadius, this.pageWidth);
      this.close();
    }
  }
};
</script>
