<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
  contact@meeds.io
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
  <v-app>
    <v-card
      color="transparent"
      class="mb-6"
      flat>
      <v-hover>
        <v-img
          slot-scope="{ hover }"
          :lazy-src="bannerUrl"
          :src="bannerUrl"
          :min-height="36"
          id="bannerImg"
          height="auto"
          min-width="100%"
          class="d-flex"
          eager>
          <div
            v-show="hover"
            class="d-flex flex-grow-1 position-absolute full-height full-width">
            <div class="me-2 ms-auto my-auto mt-sm-2 mb-sm-0">
              <v-btn
                v-if="!isDefaultBanner"
                v-show="hover"
                :title="$t('banner.title.deleteBanner')"
                id="bannerDeleteButton"
                outlined
                icon
                dark
                @click="removeBanner">
                <v-icon size="18">mdi-delete</v-icon>
              </v-btn>
              <v-btn
                v-show="hover"
                ref="bannerInput"
                id="bannerEditButton"
                :title="$t('banner.title.ChangeBanner')"
                icon
                outlined
                dark
                @click="$refs.imageCropDrawer.open()">
                <v-icon size="18">fas fa-file-image</v-icon>
              </v-btn>
            </div>
          </div>
        </v-img>
      </v-hover>
      <image-crop-drawer
        ref="imageCropDrawer"
        :crop-options="cropOptions"
        :max-file-size="maxUploadSizeInBytes"
        :src="bannerUrl"
        max-image-width="1280"
        drawer-title="banner.title.ChangeBanner"
        @input="uploadBanner" />
    </v-card>
    <layout-notification-alerts />
  </v-app>
</template>

<script>
const DEFAULT_MAX_UPLOAD_SIZE_IN_MB = 2;
const ONE_KB = 1024;

export default {
  props: {
    bannerUrl: {
      type: String,
      default: '',
    },
    fileId: {
      type: String,
      default: '',
    },
    maxUploadSize: {
      type: Number,
      default: () => DEFAULT_MAX_UPLOAD_SIZE_IN_MB,
    },
    saveSettingsURL: {
      type: String,
      default: '',
    }
  },
  data: () => ({
    cropOptions: {
      aspectRatio: 1280 / 175,
      viewMode: 1,
    },
  }),
  computed: {
    maxUploadSizeInBytes() {
      return this.maxUploadSize * ONE_KB * ONE_KB;
    },
    isDefaultBanner() {
      return this.bannerUrl && this.bannerUrl.includes('/portal/rest/v1/social/spaceTemplates/');
    },
  },
  mounted() {
    if (this.fileId === 'default') {
      this.bannerUrl = `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaceTemplates/communication/banner`;
    }
    this.$root.$applicationLoaded();
  },
  methods: {
    getBanner(uploadId) {
      return this.$bannerService.getBanner(uploadId)
        .then(data => {
          this.bannerUrl = data.bannerUrl;
          this.fileId = data.fileId;
          this.saveSettings();
        });
    },
    uploadBanner(uploadId) {
      return this.getBanner(uploadId).
        then(() => {
          this.$root.$emit('alert-message', this.$t('banner.title.BannerUpdated'), 'success');
        });
    },
    removeBanner() {
      this.bannerUrl = `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaceTemplates/communication/banner`;
      this.saveSettings();
      this.$nextTick().then (() => this.$root.$emit('alert-message', this.$t('banner.title.BannerDeleted'), 'success'));
    },
    saveSettings() {
      this.$bannerService.saveSettings(this.saveSettingsURL ,{
        fileId: this.fileId,
        bannerUrl: this.bannerUrl
      });
    }
  },

};
</script>