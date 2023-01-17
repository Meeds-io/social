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
        @click="$refs.imageCropDrawer.open()">
        {{ hasImage && $t('generalSettings.changeLoginBackground.button') || $t('generalSettings.addLoginBackground.button') }}
      </v-btn>
      <portal-general-settings-color-picker
        v-if="hasImage"
        v-model="loginBackgroundTextColor"
        :label="$t('generalSettings.loginBackgroundFontColor')" />
    </div>
    <image-crop-drawer
      ref="imageCropDrawer"
      v-model="loginBackgroundUploadId"
      :crop-options="cropOptions"
      :max-file-size="maxFileSize"
      :src="loginBackgroundPreviewSrc"
      drawer-title="generalSettings.changeLoginBackground.drawerTitle"
      @data="loginBackgroundData = $event" />
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
  },
  data: () => ({
    loginBackgroundData: null,
    loginBackgroundUploadId: null,
    loginBackgroundTextColor: '#FFFFFF',
    uploadInProgress: false,
    uploadProgress: 0,
    maxFileSize: 2097152,
    resetInput: false,
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
      this.$emit('data-updated', this.loginBackgroundData);
    },
    loginBackgroundTextColor() {
      this.$emit('text-color-updated', this.loginBackgroundTextColor);
    },
  },
  methods: {
    init(defaultData, defaultTextColor, defaultUploadId) {
      this.loginBackgroundUploadId = defaultUploadId || null;
      this.loginBackgroundData = defaultData;
      this.loginBackgroundTextColor = defaultTextColor || '#FFFFFF';
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
    },
  },
};
</script>