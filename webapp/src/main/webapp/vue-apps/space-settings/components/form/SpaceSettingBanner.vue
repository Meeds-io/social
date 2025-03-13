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
            id="spaceBannerEditButton"
            ref="bannerInput"
            icon
            outlined
            :title="$t('UIPopupBannerUploader.title.ChangeBanner')"
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
            :title="$t('UIPopupBannerUploader.title.deleteBanner')"
            @click="removeBanner">
            <v-icon size="18">
              fa-undo
            </v-icon>
          </v-btn>
        </div>
      </div>
    </div>
    <v-img
      id="spaceAvatarImg"
      class="d-flex border-radius"
      :class="!bannerUrl && 'primary'"
      contain
      eager
      height="auto"
      :lazy-src="imageData || bannerUrl || ''"
      max-width="100%"
      :src="imageData || bannerUrl || ''">
      <v-card
        class="full-width full-height"
        flat
        @click="$refs.imageCropDrawer.open()" />
    </v-img>
    <image-crop-drawer
      ref="imageCropDrawer"
      :crop-options="cropOptions"
      drawer-title="UIPopupBannerUploader.title.ChangeBanner"
      :max-file-size="maxUploadSizeInBytes"
      max-image-width="1280"
      :src="imageData || `${bannerUrl}&size=0`"
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
    },
    data: () => ({
      imageData: null,
      useDefaultBanner: false,
      cropOptions: {
        aspectRatio: 1280 / 175,
        viewMode: 1,
      },
    }),
    computed: {
      bannerUrl () {
        if (this.useDefaultBanner) {
          return this.$root.space?.template ? `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaceTemplates/${this.$root.space?.template}/banner?lastModified=${Date.now()}` : '';
        } else {
          return this.imageData || this.$root.space?.bannerUrl;
        }
      },
      isDefaultBanner () {
        return this.useDefaultBanner || !this.bannerUrl || this.bannerUrl.includes('/spaceTemplates/');
      },
      maxUploadSizeInBytes () {
        return this.maxUploadSize * 1024 * 1024;
      },
      height () {
        if (this.$root.isMobile) {
          return 125;
        } else {
          return 175;
        }
      },
    },
    methods: {
      removeBanner () {
        this.useDefaultBanner = true;
        this.imageData = null;
        this.$emit('input', 'DEFAULT_BANNER');
      },
      updateBanner (uploadId) {
        this.useDefaultBanner = false;
        this.$emit('input', uploadId);
      },
    },
  };
</script>