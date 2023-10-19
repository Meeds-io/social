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
  <image-crop-drawer
    ref="drawer"
    :drawer-title="$t('attachment.imageCropDrawer.title')"
    :src="imageCropperSrc"
    :max-file-size="maxFileSize"
    :crop-options="cropOptions"
    :can-upload="canUpload"
    :back-icon="backIcon"
    :use-format="useFormat"
    alt
    @input="uploadId = $event"
    @data="imageData = $event"
    @alt-text="altText = $event"
    @format="format = $event" />
</template>
<script>
export default {
  props: {
    cropOptions: {
      type: Object,
      default: () => ({
        aspectRatio: 1,
        viewMode: 1,
      }),
    },
    canUpload: {
      type: Boolean,
      default: false,
    },
    useFormat: {
      type: Boolean,
      default: false,
    },
    backIcon: {
      type: Boolean,
      default: false,
    },
    embedded: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    imageItem: null,
    uploadId: null,
    maxFileSize: 20971520,
    altText: null,
    format: null,
    imageData: null
  }),
  computed: {
    imageCropperSrc() {
      let imageSrc = this.imageItem?.src || '';
      if (imageSrc.length) {
        imageSrc = imageSrc.split('?')[0];
      }
      return imageSrc;
    },
    imageMimeType() {
      return this.imageItem?.mimetype || this.imageItem?.data &&  this.$refs.drawer.getBase64Mimetype(this.imageItem.data) || '';
    }
  },
  watch: {
    imageData() {
      this.updateImageData();
    },
    altText() {
      if (this.imageMimeType === 'image/gif') {
        this.updateImageData();
      }
    },
  },
  created() {
    document.addEventListener('attachments-image-open-crop-drawer', this.openAttachmentCropDrawer);
  },
  beforeDestroy() {
    document.removeEventListener('attachments-image-open-crop-drawer', this.openAttachmentCropDrawer);
  },
  methods: {
    openAttachmentCropDrawer(event) {
      this.open(event?.detail);
    },
    open(imageItem) {
      this.imageItem = imageItem;
      this.$refs.drawer.open(this.imageItem);
    },
    updateImageData() {
      if (this.embedded) {
        this.$emit('update', {
          src: this.imageData || '',
          uploadId: this.uploadId,
          id: this.imageItem?.id || '',
          progress: 100,
          oldUploadId: this.imageItem?.uploadId || '',
          altText: this.altText || '',
          format: this.format || '',
          mimetype: this.imageMimeType
        });
      } else {
        document.dispatchEvent(new CustomEvent('attachment-update', {detail: {
          src: this.imageData || '',
          uploadId: this.uploadId,
          id: this.imageItem?.id || '',
          progress: 100,
          oldUploadId: this.imageItem?.uploadId || '',
          altText: this.altText || '',
          format: this.format || '',
          mimetype: this.imageMimeType
        }}));
      }
    },
  },
};
</script>
