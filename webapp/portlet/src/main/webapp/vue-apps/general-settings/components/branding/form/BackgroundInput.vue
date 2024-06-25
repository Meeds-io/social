<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
  <div>
    <div class="d-flex align-center">
      <v-switch
        v-model="enabled"
        class="my-auto me-n2">
        <template #label>
          <slot name="title"></slot>
        </template>
      </v-switch>
    </div>
    <v-list-item
      v-if="enabled"
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto">
        <v-radio-group
          v-model="choice"
          class="my-auto text-no-wrap flex-grow-1 flex-shrink-0"
          mandatory>
          <v-radio
            value="color"
            class="ma-0">
            <template #label>
              <span class="text-font-size text-color">{{ $t('generalSettings.color') }}</span>
            </template>
          </v-radio>
          <div v-if="choice === 'color'" class="mx-auto pe-6">
            <portal-general-settings-color-picker
              v-model="branding.pageBackgroundColor"
              class="my-auto" />
          </div>
          <div class="d-flex align-center">
            <v-radio
              value="image"
              class="mx-0 mt-2">
              <template #label>
                <div class="d-flex align-center">
                  <span class="text-font-size text-color">{{ $t('generalSettings.image') }}</span>
                </div>
              </template>
            </v-radio>
            <portal-general-settings-background-image-attachment
              v-if="choice === 'image'"
              v-model="backgroundImageUploadId"
              ref="backgroundImage"
              :has-file="hasFile"
              class="my-auto ms-4"
              @image-data-updated="setPageBackgroundData"
              @reset="deletePageBackground" />
          </div>
        </v-radio-group>
      </v-list-item-content>
    </v-list-item>
    <div v-if="hasFile" class="d-flex">
      <v-radio-group
        v-model="backgroundImageStyle"
        class="my-auto text-no-wrap flex-grow-1 flex-shrink-0"
        mandatory>
        <v-radio
          value="cover"
          class="mx-0">
          <template #label>
            <span class="text-font-size text-color">{{ $t('generalSettings.imageSizeCover') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="contain"
          class="mx-0">
          <template #label>
            <span class="text-font-size text-color">{{ $t('generalSettings.imageSizeContain') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="repeat"
          class="mx-0">
          <template #label>
            <span class="text-font-size text-color">{{ $t('generalSettings.imageRepeat') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="no-repeat"
          class="mx-0">
          <template #label>
            <span class="text-font-size text-color">{{ $t('generalSettings.imageNoRepeat') }}</span>
          </template>
        </v-radio>
      </v-radio-group>
      <v-radio-group
        v-model="branding.pageBackgroundPosition"
        class="my-auto text-no-wrap flex-grow-1 flex-shrink-0"
        mandatory>
        <v-radio
          value="top left"
          class="mx-0">
          <template #label>
            <span class="text-font-size text-color">{{ $t('generalSettings.imagePositionTopLeft') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="top right"
          class="mx-0">
          <template #label>
            <span class="text-font-size text-color">{{ $t('generalSettings.imagePositionTopRight') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="bottom left"
          class="mx-0">
          <template #label>
            <span class="text-font-size text-color">{{ $t('generalSettings.imagePositionBottomLeft') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="bottom right"
          class="mx-0">
          <template #label>
            <span class="text-font-size text-color">{{ $t('generalSettings.imagePositionBottomRight') }}</span>
          </template>
        </v-radio>
      </v-radio-group>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: Object,
      default: null,
    },
    defaultBackgroundColor: {
      type: String,
      default: () => '#FFFFFFFF',
    },
  },
  data: () => ({
    branding: null,
    enabled: false,
    choice: null,
    backgroundImageStyle: null,
    backgroundImageUploadId: null,
    initialized: false,
  }),
  computed: {
    backgroundImage() {
      return this.branding.pageBackground?.fileId;
    },
    hasFile() {
      return this.backgroundImageUploadId !== 0 && (this.backgroundImageUploadId || this.backgroundImage);
    },
  },
  watch: {
    initialized: {
      immediate: true,
      handler() {
        this.$emit('initialized');
      },
    },
    branding: {
      deep: true,
      handler() {
        if (this.branding) {
          this.$emit('input', this.branding);
        }
      },
    },
    enabled() {
      if (this.initialized) {
        this.branding.pageBackgroundColor = this.enabled && this.defaultBackgroundColor || null;
        this.branding.pageBackground = {};
        this.backgroundImageStyle = null;
      }
    },
    backgroundImageStyle() {
      if (this.initialized) {
        if (this.backgroundImageStyle === 'cover' || this.backgroundImageStyle === 'contain') {
          this.branding.pageBackgroundSize = this.backgroundImageStyle;
          this.branding.pageBackgroundRepeat = null;
        } else {
          this.branding.pageBackgroundSize = null;
          this.branding.pageBackgroundRepeat = this.backgroundImageStyle;
        }
      }
    },
    backgroundImageUploadId() {
      if (this.initialized && this.branding.pageBackground) {
        this.branding.pageBackground.uploadId = this.backgroundImageUploadId || 0;
        this.branding.pageBackground.fileId = 0;
      }
    },
    choice() {
      if (this.initialized) {
        if (this.choice === 'color') {
          this.branding.pageBackgroundColor = this.defaultBackgroundColor;
          this.branding.pageBackground = {
            data: null,
            fileId: 0,
            uploadId: 0,
          };
          this.backgroundImageUploadId = 0;
          this.backgroundImageStyle = null;
        } else if (this.choice === 'image') {
          this.branding.pageBackgroundColor = null;
          this.branding.pageBackground = {};
          this.backgroundImageUploadId = null;
        }
      }
    },
  },
  created() {
    this.branding = this.value;
    if (this.branding.pageBackgroundSize || this.branding.pageBackgroundRepeat) {
      if (this.branding.pageBackgroundSize === 'cover'
          || this.branding.pageBackgroundSize === 'contain') {
        this.backgroundImageStyle = this.branding.pageBackgroundSize;
      } else {
        this.backgroundImageStyle = this.branding.pageBackgroundRepeat;
      }
    }
    if (this.branding.pageBackground?.fileId) {
      this.choice = 'image';
    } else {
      this.choice = 'color';
    }
    this.enabled = !!this.branding.pageBackgroundColor || !!this.branding.pageBackground.fileId;
    this.$nextTick().then(() => this.initialized = true);
  },
  methods: {
    deletePageBackground() {
      this.branding.pageBackground = {
        data: null,
        fileId: 0,
        uploadId: 0,
      };
    },
    setPageBackgroundData(data) {
      this.branding.pageBackground.data = data;
      this.$emit('input', this.branding);
    },
  },
};
</script>