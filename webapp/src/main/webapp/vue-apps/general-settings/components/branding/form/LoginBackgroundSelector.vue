<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <div class="d-flex">
    <div>
      <v-btn
        class="btn btn-primary"
        outlined
        @click="$refs.imageCropDrawer.open(loginBackgroundItem)">
        {{ hasImage && $t('generalSettings.changeLoginBackground.button') || $t('generalSettings.addLoginBackground.button') }}
      </v-btn>
      <portal-general-settings-color-picker
        v-if="hasImage"
        v-model="loginBackgroundTextColor"
        :label="$t('generalSettings.loginBackgroundFontColor')" />
    </div>
    <image-crop-drawer
      ref="imageCropDrawer"
      :crop-options="cropOptions"
      :max-file-size="maxFileSize"
      :src="loginBackgroundPreviewSrc"
      alt
      drawer-title="generalSettings.changeLoginBackground.drawerTitle"
      @apply="updateImage" />
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: () => null,
    },
    aspectRatio: {
      type: Number,
      default: () => 16 / 9,
    },
    defaultData: {
      type: String,
      default: null,
    },
    altText: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    loginBackgroundData: null,
    loginBackgroundUploadId: null,
    loginBackgroundTextColor: '#FFFFFF',
    loginBackgroundAltText: '',
    uploadInProgress: false,
    uploadProgress: 0,
    maxFileSize: 2097152,
    resetInput: false,
    loginBackgroundItem: {},
  }),
  computed: {
    cropOptions() {
      return {
        aspectRatio: this.aspectRatio,
        cropBoxResizable: false,
        minCropBoxHeight: 200,
      };
    },
    hasImage() {
      return this.loginBackgroundData;
    },
    loginBackgroundPreviewSrc() {
      if (this.loginBackgroundData) {
        return this.$utils.convertImageDataAsSrc(this.loginBackgroundData);
      } else {
        return this.defaultData;
      }
    },
  },
  watch: {
    loginBackgroundUploadId() {
      this.$emit('input', this.loginBackgroundUploadId || '');
    },
    loginBackgroundData() {
      this.$emit('text-alt-updated', this.loginBackgroundAltText);
      this.$emit('data-updated', this.loginBackgroundData);
    },
    loginBackgroundTextColor() {
      this.$emit('text-color-updated', this.loginBackgroundTextColor);
    },
    altText() {
      this.loginBackgroundItem.altText = this.altText;
    },
  },
  methods: {
    init(defaultData, defaultTextColor, defaultUploadId) {
      this.loginBackgroundUploadId = defaultUploadId || null;
      this.loginBackgroundData = defaultData;
      this.loginBackgroundTextColor = defaultTextColor || '#FFFFFF';
      this.loginBackgroundAltText = this.altText;
      this.loginBackgroundItem.altText = this.loginBackgroundAltText;
      if (this.$refs.imageCropDrawer) {
        this.$refs.imageCropDrawer.init();
      }
    },
    preSave(branding) {
      if (this.loginBackgroundUploadId) {
        Object.assign(branding, {
          loginBackground: {
            uploadId: this.loginBackgroundUploadId,
          },
        });
      }
      if (this.defaultData || this.loginBackgroundUploadId) {
        Object.assign(branding, {
          loginBackgroundTextColor: this.loginBackgroundTextColor,
        });
      } else {
        Object.assign(branding, {
          loginBackgroundTextColor: null,
        });
      }
      if (this.altText || this.loginBackgroundAltText) {
        Object.assign(branding, {
          loginBackgroundAltText: this.loginBackgroundAltText,
        });
      } else {
        Object.assign(branding, {
          loginBackgroundAltText: null,
        });
      }
    },
    updateImage(image) {
      this.loginBackgroundData = image.src;
      this.loginBackgroundAltText = image.altText;
      this.loginBackgroundUploadId = image.uploadId;
    }
  },
};
</script>