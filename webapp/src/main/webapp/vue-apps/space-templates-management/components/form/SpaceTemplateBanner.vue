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
    <div class="d-flex position-relative full-width mb-2">
      <help-label
        label="spaceTemplate.defaultSpaceConfigurationStepBanner"
        label-class="text-body font-weight-bold"
        tooltip="spaceTemplate.defaultSpaceConfigurationStepBannerTooltip"
        class="font-weight-bold flex-grow-1 flex-shrink-1">
        <template #helpContent>
          <p>
            {{ $t('spaceTemplate.defaultSpaceConfigurationStepBannerHelp1') }}
          </p>
          <p>
            {{ $t('spaceTemplate.defaultSpaceConfigurationStepBannerHelp2') }}
          </p>
        </template>
      </help-label>
      <div class="position-relative px-7">
        <v-btn
          :title="$t('spaceTemplate.defaultSpaceConfigurationStepBannerButtonTooltip')"
          class="absolute-vertical-center"
          outlined
          icon
          @click="$refs.imageCropDrawer.open()">
          <v-icon size="18">fa-camera</v-icon>
        </v-btn>
      </div>
    </div>
    <img
      :src="bannerUrl"
      :alt="$t('spaceTemplate.bannerImage')"
      width="100%"
      height="auto"
      class="d-flex border-radius">
    <image-crop-drawer
      ref="imageCropDrawer"
      v-model="uploadId"
      :crop-options="cropOptions"
      :max-file-size="maxUploadSizeInBytes"
      :src="bannerUrl"
      max-image-width="1280"
      drawer-title="spaceTemplate.defaultSpaceConfigurationStepBannerCropperDrawer"
      @data="imageData = $event" />
  </div>
</template>
<script>
export default {
  props: {
    spaceTemplate: {
      type: Object,
      default: null,
    },
    maxUploadSize: {
      type: Number,
      default: () => 2,
    },
  },
  data: () => ({
    imageData: null,
    uploadId: null,
    defaultBannerSrc: '/social/images/defaultSpaceBanner.webp',
    cropOptions: {
      aspectRatio: 1280 / 175,
      viewMode: 1,
    },
  }),
  computed: {
    spaceTemplateId() {
      return this.spaceTemplate?.id;
    },
    bannerFileId() {
      return this.spaceTemplate?.bannerFileId;
    },
    bannerUrl() {
      if (this.imageData) {
        return this.imageData;
      } else if (this.bannerFileId) {
        return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/spaceTemplateBanner/${this.spaceTemplateId}/${this.bannerFileId}`;
      } else {
        return this.defaultBannerSrc;
      }
    },
    maxUploadSizeInBytes() {
      return this.maxUploadSize * 1024 * 1024;
    },
    height() {
      if (this.$root.isMobile) {
        return 125;
      } else {
        return 175;
      }
    },
  },
  methods: {
    save(spaceTemplateId) {
      if (this.uploadId) {
        return this.$fileAttachmentService.saveAttachments({
          objectType: 'spaceTemplateBanner',
          objectId: this.spaceTemplateId || spaceTemplateId,
          uploadedFiles: this.uploadId && [{uploadId: this.uploadId}] || [],
          attachedFiles: [],
        }).then((report) => {
          if (report?.errorByUploadId?.length) {
            const attachmentHtmlError = Object.values(report.errorByUploadId).join('<br>');
            this.$root.$emit('alert-message-html', attachmentHtmlError, 'error');
          } else if (this.$refs.uploadInput) {
            this.$refs.uploadInput.reset();
          }
        });
      }
    },
  },
};
</script>