/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
<template>
  <image-crop-drawer
    ref="attachedImageCropDrawer"
    :drawer-title="$t('attachment.imageCropDrawer.title')"
    :src="imageCropperSrc"
    :max-file-size="maxFileSize"
    :crop-options="cropOptions"
    :can-upload="false"
    :default-alt-text="imageAltText"
    alt
    @input="uploadId = $event"
    @data="updateImageData($event)"
    @alt-text="altText = $event" />
</template>
<script>
export default {
  data () {
    return {
      imageItem: null,
      cropOptions: {
        aspectRatio: 1,
        viewMode: 1,
      },
      uploadId: null,
      maxFileSize: 20971520,
      altText: null,
      
    };
  },
  computed: {
    imageCropperSrc() {
      return this.imageItem?.src;
    },
    imageAltText() {
      return this.imageItem?.altText || '';
    }
  },
  created() {
    document.addEventListener('attachments-image-open-crop-drawer', this.openAttachmentCropDrawer);
  },
  methods: {
    openAttachmentCropDrawer(event) {
      if (event?.detail) {
        this.imageItem = event?.detail;
        this.$refs.attachedImageCropDrawer.open(this.imageCropperSrc, this.imageAltText);
      }
    },
    updateImageData(imageData) {
      if (imageData) {
        document.dispatchEvent(new CustomEvent('attachment-update', {detail: { 
          src: imageData,
          uploadId: this.uploadId,
          id: this.imageItem?.id ? this.imageItem?.id : '',
          progress: 100,
          oldUploadId: this.imageItem?.uploadId?  this.imageItem?.uploadId : '',
          altText: this.altText ? this.altText : ''
        }}));
      }
    },
  },
};
</script>
