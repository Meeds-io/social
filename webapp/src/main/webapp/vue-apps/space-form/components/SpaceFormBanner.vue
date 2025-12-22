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
  <div class="full-width border-box-sizing">
    <div class="d-flex position-relative my-4">
      <div class="flex-grow-0 text-header">
        {{ $t('spacesList.label.bannerLabel') }}
      </div>
      <div class="flex-grow-1 ms-2 position-relative">
        <div class="absolute-vertical-center">
          <v-btn
            ref="bannerInput"
            id="spaceBannerEditButton"
            :title="$t('spacesList.label.changeBanner')"
            outlined
            icon
            @click="$refs.imageCropDrawer.open()">
            <v-icon size="18">fa-camera</v-icon>
          </v-btn>
          <v-btn
            v-show="!isDefaultBanner"
            :title="$t('spacesList.label.deleteBanner')"
            id="spaceBannerDeleteButton"
            outlined
            icon
            @click="removeBanner">
            <v-icon size="18">fa-undo</v-icon>
          </v-btn>
        </div>
      </div>
    </div>
    <img
      :src="bannerUrl"
      :class="!bannerUrl && 'primary'"
      width="100%"
      height="50px"
      class="border-radius clickable"
      @click="$refs.imageCropDrawer.open()">
    <image-crop-drawer
      ref="imageCropDrawer"
      :crop-options="cropOptions"
      :max-file-size="maxUploadSizeInBytes"
      :src="bannerUrl"
      max-image-width="1280"
      drawer-title="UIPopupBannerUploader.title.ChangeBanner"
      @apply="updateImage" />
  </div>
</template>
<script>
export default {
  props: {
    maxUploadSize: {
      type: Number,
      default: () => 2,
    },
    defaultBannerUrl: {
      type: String,
      default: null,
    },
    src: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    imageData: null,
    cropOptions: {
      aspectRatio: 1280 / 175,
      viewMode: 1,
    },
  }),
  computed: {
    bannerUrl() {
      return this.imageData || this.defaultBannerUrl;
    },
    isDefaultBanner() {
      return !this.imageData;
    },
    maxUploadSizeInBytes() {
      return this.maxUploadSize * 1024 * 1024;
    },
    height() {
      if (this.$vuetify.mobile) {
        return 125;
      } else {
        return 175;
      }
    },
  },
  created() {
    this.imageData = this.src;
  },
  methods: {
    removeBanner() {
      this.imageData = null;
      if (this.src) {
        this.$emit('input', 'DEFAULT_BANNER');
      } else {
        this.$emit('input', null);
      }
    },
    updateBanner(uploadId) {
      this.$emit('input', uploadId);
    },
    updateImage(image) {
      this.imageData = image.src;
      this.updateBanner(image.uploadId);
    }
  },
};
</script>