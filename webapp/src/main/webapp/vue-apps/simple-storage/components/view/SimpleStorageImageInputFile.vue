<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->

<template>
  <v-file-input
    ref="fileInput"
    accept=".png,.jpg,.jpeg,.webp,.svg,.gif,.tiff,.bmp"
    class="d-none position-absolute"
    multiple
    hide-input
    @change="handleFileChange" />
</template>

<script>

export default {
  data() {
    return {
      maxImageSize: 20971520, // 20MB
      uploadQueue: [],
      isUploading: false,
      objectType: 'public',
      objectId: 'images'
    };
  },
  created() {
    this.$root.$on('handle-upload-images', this.handleFileChange);
    window.addEventListener('beforeunload', this.preventExitIfUploading);
  },
  beforeDestroy() {
    window.removeEventListener('beforeunload', this.preventExitIfUploading);
  },
  methods: {
    openFileExplorer() {
      this.$refs.fileInput.$el.querySelector('input').click();
    },
    handleFileChange(files) {
      if (files.length) {
        const newImages = Array.from(files).map(file => {
          if (file.size > this.maxImageSize) {
            this.$root.$emit('alert-message', this.$t('simpleStorage.size.upload.error.message'), 'error');
            return;
          }
          return {
            file,
            thumbnail: null,
            name: file.name,
            size: file.size,
            creationDate: new Date().getTime(),
            progress: 0,
            error: null,
            uploadId: this.generateUploadId()
          };
        });
        newImages.forEach(image => {
          const reader = new FileReader();
          reader.onloadend = () => { image.thumbnail = reader.result; };
          reader.readAsDataURL(image.file);
        });
        this.$emit('update-images', newImages);
        this.uploadQueue.push(...newImages);
        this.processUploadQueue();
        this.$refs.fileInput.$el.querySelector('input').value = '';
      }
    },
    generateUploadId() {
      return crypto.randomUUID();
    },
    async processUploadQueue() {
      this.isUploading = true;
      const processNext = async () => {
        if (!this.uploadQueue.length) {
          return;
        }
        const image = this.uploadQueue.shift();
        this.uploadFileToServer(image);
        await processNext();
      };
      await processNext();
    },
    uploadFileToServer(image) {
      this.$uploadService.upload(image.file, image.uploadId).catch((error) => {
        image.error = error;
        this.$root.$emit('alert-message', this.$t('simpleStorage.upload.image.error.message'), 'error');
        console.error('Upload failed:', error);
      });
      this.trackUploadProgress(image);
    },
    trackUploadProgress(image) {
      const interval = setInterval(() => {
        this.$uploadService.getUploadProgress(image.uploadId).then((percent) => {
          image.progress = Number(percent);
          if (image.progress >= 100) {
            clearInterval(interval);
            this.isUploading = this.uploadQueue.length;
            this.saveImageAttachment(image.uploadId);
          }
        }).catch(() => {
          clearInterval(interval);
        });
      }, 200);
    },
    async saveImageAttachment(uploadId) {
      await this.$fileAttachmentService.createAttachment({
        fileAttachmentObject: { uploadId },
        objectType: this.objectType,
        objectId: this.objectId
      }).then(image => {
        this.$root.$emit('image-attachment-saved', image, uploadId);
        this.$root.$emit('alert-message', this.$t('simpleStorage.save.image.success.message'), 'success');
      }).catch(() => {
        this.$uploadService.deleteUpload(uploadId);
        this.$root.$emit('alert-message',  this.$t('simpleStorage.save.image.error.message'), 'error');
      });
    },
    preventExitIfUploading(event) {
      if (this.isUploading) {
        event.preventDefault();
        event.returnValue = '';
      }
    }
  }
};
</script>
