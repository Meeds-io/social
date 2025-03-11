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
            id="spaceBannerEditButton"
            ref="bannerInput"
            icon
            outlined
            :title="$t('spacesList.label.changeBanner')"
            @click="$refs.imageCropDrawer.open()">
            <v-icon size="18">
              fa-camera
            </v-icon>
          </v-btn>
          <v-btn
            v-show="!isDefaultBanner"
            id="spaceBannerDeleteButton"
            icon
            outlined
            :title="$t('spacesList.label.deleteBanner')"
            @click="removeBanner">
            <v-icon size="18">
              fa-undo
            </v-icon>
          </v-btn>
        </div>
      </div>
    </div>
    <img
      class="border-radius clickable"
      :class="!bannerUrl && 'primary'"
      height="50px"
      :src="bannerUrl"
      width="100%"
      @click="$refs.imageCropDrawer.open()">
    <image-crop-drawer
      ref="imageCropDrawer"
      :crop-options="cropOptions"
      drawer-title="UIPopupBannerUploader.title.ChangeBanner"
      :max-file-size="maxUploadSizeInBytes"
      max-image-width="1280"
      :src="bannerUrl"
      @data="imageData = $event"
      @input="updateBanner" />
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
    },
    data: () => ({
      imageData: null,
      cropOptions: {
        aspectRatio: 1280 / 175,
        viewMode: 1,
      },
    }),
    computed: {
      bannerUrl () {
        return this.imageData || this.defaultBannerUrl;
      },
      isDefaultBanner () {
        return !this.imageData;
      },
      maxUploadSizeInBytes () {
        return this.maxUploadSize * 1024 * 1024;
      },
      height () {
        if (this.$vuetify.mobile) {
          return 125;
        } else {
          return 175;
        }
      },
    },
    methods: {
      removeBanner () {
        this.imageData = null;
        this.$emit('input', null);
      },
      updateBanner (uploadId) {
        this.$emit('input', uploadId);
      },
    },
  };
</script>