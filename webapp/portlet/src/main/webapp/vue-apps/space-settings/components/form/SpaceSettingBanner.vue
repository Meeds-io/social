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
  <div class="full-width border-box-sizing pe-0 pe-sm-8">
    <div class="text-header d-flex position-relative my-4">
      <help-label
        label="SpaceSettings.label.banner"
        label-class="text-header"
        tooltip="SpaceSettings.label.bannerTooltip">
        <template #helpContent>
          <p>
            {{ $t('SpaceSettings.label.bannerHelp1') }}
          </p>
          <p>
            {{ $t('SpaceSettings.label.bannerHelp2') }}
          </p>
        </template>
      </help-label>
      <div class="flex-grow-1 ms-2 position-relative">
        <div class="absolute-vertical-center">
          <v-btn
            ref="bannerInput"
            id="spaceBannerEditButton"
            :title="$t('UIPopupBannerUploader.title.ChangeBanner')"
            icon
            outlined
            @click="$refs.imageCropDrawer.open()">
            <v-icon size="18">fa-camera</v-icon>
          </v-btn>
        </div>
      </div>
    </div>
    <v-img
      :lazy-src="imageData || bannerUrl || ''"
      :src="imageData || bannerUrl || ''"
      :class="!bannerUrl && 'primary'"
      id="spaceAvatarImg"
      max-width="100%"
      height="auto"
      class="d-flex border-radius"
      contain
      eager>
      <v-card
        class="full-width full-height"
        flat
        @click="$refs.imageCropDrawer.open()" />
    </v-img>
    <image-crop-drawer
      ref="imageCropDrawer"
      :crop-options="cropOptions"
      :max-file-size="maxUploadSizeInBytes"
      :src="imageData || `${bannerUrl}&size=0`"
      max-image-width="1280"
      drawer-title="UIPopupBannerUploader.title.ChangeBanner"
      @data="imageData = $event"
      @input="$emit('input', $event)" />
  </div>
</template>
<script>
export default {
  props: {
    maxUploadSize: {
      type: Number,
      default: () => 2,
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
      return this.$root.space?.bannerUrl;
    },
    isDefaultBanner() {
      return !this.bannerUrl || this.bannerUrl.includes('/spaceTemplates/');
    },
    maxUploadSizeInBytes() {
      return this.maxUploadSize * 1024 * 1024;
    },
    height() {
      if (this.isMobile) {
        return 125;
      } else {
        return 175;
      }
    },
    isMobile() {
      return this.$vuetify.breakpoint.mobile;
    },
  },
};
</script>