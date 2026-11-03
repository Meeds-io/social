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
    accept=".png,.jpg,.jpeg,.webp,.gif,.bmp"
    class="d-none position-absolute"
    multiple
    hide-input
    @change="handleFileChange" />
</template>

<script>
// The message UploadService.getUploadProgress throws when the server reports the upload as failed
const UPLOAD_FAILED_MESSAGE = 'Upload error';

export default {
  data() {
    return {
      allowedExtensions: ['png', 'jpg', 'jpeg', 'webp', 'gif', 'bmp'],
      maxImageSize: 20971520, // 20MB
      uploadQueue: [],
      isUploading: false,
      pendingUploads: [],
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
      // Cleared when the picker opens rather than after a pick, and never by
      // this code while the request of an image still in the list is pending:
      // on iOS Safari a picked photo may not stay readable once its input is
      // cleared (inferred, not reproduced on a device).
      const input = this.$refs.fileInput.$el.querySelector('input');
      if (!this.pendingUploads.some(image => !image.error)) {
        input.value = '';
      }
      input.click();
    },
    handleFileChange(files) {
      if (files.length) {
        const newImages = Array.from(files).map(file => {
          const fileExtension = file.name.split('.').pop().toLowerCase();
          if (!file.type.startsWith('image/') || !this.allowedExtensions.includes(fileExtension)) {
            this.$root.$emit('alert-message', this.$t('simpleStorage.invalid.format.error.message'), 'error');
            return;
          }
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
      image.uploadPending = true;
      this.pendingUploads.push(image);
      Promise.resolve().then(() => this.$uploadService.upload(image.file, image.uploadId))
        .then(uploadId => {
          if (!uploadId) {
            throw new Error('Upload request failed');
          }
          image.uploadAnswered = true;
        })
        .catch(error => this.handleUploadError(image, error))
        .finally(() => {
          image.uploadPending = false;
          this.pendingUploads.splice(this.pendingUploads.indexOf(image), 1);
        });
      this.trackUploadProgress(image);
    },
    trackUploadProgress(image) {
      window.setTimeout(() => {
        if (image.error) {
          return;
        }
        const sentWhileUploadPending = image.uploadPending;
        const sentAfterUploadAnswered = image.uploadAnswered;
        this.$uploadService.getUploadProgress(image.uploadId).then((percent) => {
          if (image.error) {
            return;
          }
          // A poll reads 100 once the body has arrived, before the server has
          // written and validated the file: only a poll sent after the upload
          // request answered completes the image, and it saves it once.
          image.progress = sentAfterUploadAnswered ? Number(percent) : Math.min(Number(percent), 99);
          if (image.progress >= 100) {
            this.isUploading = this.uploadQueue.length;
            this.saveImageAttachment(image.uploadId);
          } else {
            this.trackUploadProgress(image);
          }
        }).catch((error) => {
          // The server knows an upload only once its request reaches it, which
          // can be well after the first poll: until the request settles, a
          // poll that finds no upload is not a failure.
          if (sentWhileUploadPending && error?.message !== UPLOAD_FAILED_MESSAGE) {
            this.trackUploadProgress(image);
          } else {
            this.handleUploadError(image, error);
          }
        });
      }, 200);
    },
    handleUploadError(image, error) {
      if (image.error) {
        return;
      }
      image.error = error;
      this.isUploading = this.uploadQueue.length;
      this.$root.$emit('image-attachment-upload-failed', image.uploadId);
      this.$root.$emit('alert-message', this.$t('simpleStorage.upload.image.error.message'), 'error');
      console.error('Upload failed:', error);
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
        this.$root.$emit('image-attachment-upload-failed', uploadId);
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
