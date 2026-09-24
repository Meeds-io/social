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
          <styling-section-margin-input
            v-if="initialized"
            v-model="pageMargins"
            :label="$t('generalSettings.page.margins.label')"
            :min="0"
            :max="80"
            class="mt-2"
            top
            right
            bottom
            left />
          <span class="text-header mt-2">
            {{ $t('generalSettings.page.background') }}
          </span>
          <portal-general-settings-background-input
            v-if="initialized"
            v-model="backgroundProperties"
            :default-background-color="defaultPageBackground"
            :image-path="pageBackgroundPath"
            class="mt-2 pe-3"
            scrolling />
        </div>
        <div class="mt-2 pe-4 d-flex flex-column">
          <span class="text-title">
            {{ $t('generalSettings.application.styling.label') }}
          </span>
          <p class="text-subtitle mt-1 mb-0">
            {{ $t('generalSettings.application.styling.help') }}
          </p>
          <styling-margin-input
            v-if="initialized"
            v-model="applicationStyling.container"
            :min="0"
            :max="80"
            class="mt-4" />
          <styling-border-input
            v-if="initialized"
            v-model="applicationStyling.container"
            class="mt-4" />
          <styling-border-radius-input
            v-if="initialized"
            v-model="applicationStyling.container"
            class="mt-4" />
          <styling-background-input
            v-if="initialized"
            v-model="applicationStyling.container"
            :default-background-color="defaultApplicationBackground"
            class="mt-4"
            no-layer-options>
            <template #image>
              <portal-general-settings-background-image-attachment
                v-model="appBackgroundUploadId"
                :has-file="hasAppBackgroundFile"
                class="ms-auto my-auto me-n2"
                @image-data-updated="setApplicationImage('appBackground', 'backgroundImage', $event)"
                @reset="resetApplicationImage('appBackground', 'backgroundImage')" />
            </template>
          </styling-background-input>
          <styling-text-input
            v-if="initialized"
            v-model="applicationStyling.container"
            class="mt-4">
            <template #title-background-image>
              <portal-general-settings-background-image-attachment
                v-model="appTextTitleBackgroundUploadId"
                :has-file="hasAppTextTitleBackgroundFile"
                class="ms-auto my-auto me-n2"
                @image-data-updated="setApplicationImage('appTextTitleBackground', 'textTitleBackgroundImage', $event)"
                @reset="resetApplicationImage('appTextTitleBackground', 'textTitleBackgroundImage')" />
            </template>
            <template #header-background-image>
              <portal-general-settings-background-image-attachment
                v-model="appTextHeaderBackgroundUploadId"
                :has-file="hasAppTextHeaderBackgroundFile"
                class="ms-auto my-auto me-n2"
                @image-data-updated="setApplicationImage('appTextHeaderBackground', 'textHeaderBackgroundImage', $event)"
                @reset="resetApplicationImage('appTextHeaderBackground', 'textHeaderBackgroundImage')" />
            </template>
          </styling-text-input>
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
/**
 * Pages & Apps style: Page Design (width, margins, background) and the
 * platform-wide Application Styling, edited with the shared styling inputs
 * (social stylingInputs module) on a container-shaped object; the branding
 * root component maps that object to the theme variables.
 */
export default {
  data: () => ({
    drawer: false,
    backgroundProperties: null,
    defaultCustomPageWidth: '1320',
    defaultPageBackground: '#F0F0F0FF',
    defaultApplicationBackground: '#FFFFFFFF',
    pageWidth: null,
    pageMargins: null,
    applicationStyling: null,
    appBackgroundUploadId: null,
    appTextTitleBackgroundUploadId: null,
    appTextHeaderBackgroundUploadId: null,
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
      return JSON.stringify(this.backgroundProperties) === JSON.stringify(this.defaultPageStylingProperties.backgroundProperties)
          && JSON.stringify(this.applicationStyling) === JSON.stringify(this.defaultPageStylingProperties.applicationStyling)
          && JSON.stringify(this.pageMargins || null) === JSON.stringify(this.defaultPageStylingProperties.pageMargins || null)
          && this.defaultPageStylingProperties.pageWidth === this.pageWidth;
    },
    pageBackgroundPath() {
      const background = this.pageStylingProperties?.pageBackground;
      return background?.fileId && `/portal/rest/v1/platform/branding/pageBackground?v=${background.updatedDate || 0}` || null;
    },
    hasAppBackgroundFile() {
      return this.hasFile(this.appBackgroundUploadId, this.applicationStyling?.appBackground);
    },
    hasAppTextTitleBackgroundFile() {
      return this.hasFile(this.appTextTitleBackgroundUploadId, this.applicationStyling?.appTextTitleBackground);
    },
    hasAppTextHeaderBackgroundFile() {
      return this.hasFile(this.appTextHeaderBackgroundUploadId, this.applicationStyling?.appTextHeaderBackground);
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
    },
    appBackgroundUploadId() {
      this.setUploadId('appBackground', this.appBackgroundUploadId);
    },
    appTextTitleBackgroundUploadId() {
      this.setUploadId('appTextTitleBackground', this.appTextTitleBackgroundUploadId);
    },
    appTextHeaderBackgroundUploadId() {
      this.setUploadId('appTextHeaderBackground', this.appTextHeaderBackgroundUploadId);
    },
    // A group switched off (or its image removed) by the shared input clears the image field:
    // the stored file is then removed on save (upload id 0 = reset)
    'applicationStyling.container.backgroundImage'(image) {
      this.clearFileWithoutImage('appBackground', image);
    },
    'applicationStyling.container.textTitleBackgroundImage'(image) {
      this.clearFileWithoutImage('appTextTitleBackground', image);
    },
    'applicationStyling.container.textHeaderBackgroundImage'(image) {
      this.clearFileWithoutImage('appTextHeaderBackground', image);
    },
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
        backgroundAttachment: this.pageStylingProperties?.pageBackgroundAttachment || null,
        backgroundEffect: this.getPageBackgroundEffect()
      };
      this.pageWidth = this.pageStylingProperties.pageWidth;
      this.pageMargins = this.pageStylingProperties.pageMargins && JSON.parse(JSON.stringify(this.pageStylingProperties.pageMargins)) || {
        marginTop: null,
        marginRight: null,
        marginBottom: null,
        marginLeft: null,
      };
      // deep copy: the shared inputs write into the object, Cancel must leave the root untouched
      this.applicationStyling = JSON.parse(JSON.stringify(this.pageStylingProperties.applicationStyling || {}));
      this.applicationStyling.container = this.applicationStyling.container || {};
      ['appBackground', 'appTextTitleBackground', 'appTextHeaderBackground'].forEach(file => {
        this.applicationStyling[file] = this.applicationStyling[file] || this.emptyFile();
      });
      this.appBackgroundUploadId = this.applicationStyling.appBackground.uploadId || null;
      this.appTextTitleBackgroundUploadId = this.applicationStyling.appTextTitleBackground.uploadId || null;
      this.appTextHeaderBackgroundUploadId = this.applicationStyling.appTextHeaderBackground.uploadId || null;
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
        backgroundProperties: JSON.parse(JSON.stringify(this.backgroundProperties)),
        pageWidth: this.pageWidth,
        pageMargins: JSON.parse(JSON.stringify(this.pageMargins)),
        applicationStyling: JSON.parse(JSON.stringify(this.applicationStyling)),
      };
      this.initialized = true;
    },
    reset() {
      this.backgroundProperties = null;
      this.pageWidth = null;
      this.pageMargins = null;
      this.applicationStyling = null;
      this.appBackgroundUploadId = null;
      this.appTextTitleBackgroundUploadId = null;
      this.appTextHeaderBackgroundUploadId = null;
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
    emptyFile() {
      return {
        data: null,
        fileId: 0,
        updatedDate: 0,
        uploadId: 0,
      };
    },
    hasFile(uploadId, file) {
      return uploadId !== 0 && (uploadId || (file && file.fileId !== 0)) || false;
    },
    clearFileWithoutImage(fileKey, image) {
      const file = this.applicationStyling?.[fileKey];
      if (this.initialized && !image && file && (file.fileId || file.uploadId)) {
        this.applicationStyling[fileKey] = this.emptyFile();
        this[`${fileKey}UploadId`] = 0;
      }
    },
    setUploadId(fileKey, uploadId) {
      if (this.initialized && this.applicationStyling?.[fileKey]) {
        this.applicationStyling[fileKey].uploadId = uploadId || 0;
        this.applicationStyling[fileKey].fileId = 0;
      }
    },
    // An uploaded image: keep its preview on the file and show it to the shared input so that the image options appear
    setApplicationImage(fileKey, imageField, data) {
      this.applicationStyling[fileKey].data = data;
      this.$set(this.applicationStyling.container, imageField, data);
    },
    resetApplicationImage(fileKey, imageField) {
      this.applicationStyling[fileKey] = this.emptyFile();
      this.$set(this.applicationStyling.container, imageField, null);
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
      const pageMargins = this.pageMargins && Object.values(this.pageMargins).some(value => value === 0 || value) && this.pageMargins || null;
      this.$root.$emit('update-page-styling-properties',
        this.backgroundProperties,
        this.pageWidth,
        JSON.parse(JSON.stringify(this.applicationStyling)),
        pageMargins && JSON.parse(JSON.stringify(pageMargins)) || null);
      this.close();
    }
  }
};
</script>
