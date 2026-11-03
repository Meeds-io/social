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
  <v-file-input
    v-if="display"
    id="attachedImagesField"
    ref="attachedImages"
    accept="image/*"
    class="hidden"
    clearable
    multiple
    @change="uploadFiles" />
</template>
<script>
const UPLOAD_ERROR_KEY = 'attachment.errorUpload.label';
// The message UploadService.getUploadProgress throws when the server reports the upload as failed
const UPLOAD_FAILED_MESSAGE = 'Upload error';

export default {
  props: {
    maxFileSize: {
      type: Number,
      default: () => 20971520,
    },
    disablePaste: {
      type: Boolean,
      default: false
    },
  },
  data: () => ({
    filesArray: [],
    display: true,
    pendingUploads: [],
  }),
  watch: {
    filesArray() {
      this.$emit('update:images', this.filesArray);
    },
  },
  created() {
    document.addEventListener('attachments-image-open-file-explorer', this.triggerFileClickEvent);
    if (!this.disablePaste) {
      document.addEventListener('paste', this.handlePasteFiles);
    }
    document.addEventListener('attachments-drop-files', this.handleDropFiles);
  },
  beforeDestroy() {
    document.removeEventListener('attachments-image-open-file-explorer', this.triggerFileClickEvent);
    document.removeEventListener('paste', this.handlePasteFiles);
    document.removeEventListener('attachments-drop-files', this.handleDropFiles);
  },
  methods: {
    async reset() {
      // an image dropped here stops its polling and frees the picker
      this.filesArray.forEach(fileDetails => fileDetails.removed = true);
      this.filesArray = [];
      await this.refresh();
    },
    async refresh() {
      this.display = false;
      await this.$nextTick();
      this.display = true;
    },
    deleteFile(uploadId) {
      const fileIndex = this.filesArray.findIndex(f => f.uploadId === uploadId);
      this.filesArray.splice(fileIndex, 1);
      this.$uploadService.deleteUpload(uploadId);
    },
    triggerFileClickEvent() {
      const input = document.getElementById('attachedImagesField');
      // Every mounted instance hears the event and the id is the same in
      // each: only the instance owning the input found acts on it.
      if (!input || !this.$el?.contains?.(input)) {
        return;
      }
      // Cleared here, when the picker opens, rather than re-created after a
      // pick, so that picking the same image again is a change. This code
      // never clears it while the request of an image still in the list is
      // pending: on iOS Safari a picked photo may not stay readable once its
      // input is cleared (inferred, not reproduced on a device).
      if (!this.pendingUploads.some(fileDetails => !fileDetails.removed)) {
        input.value = '';
      }
      input.click();
    },
    uploadFiles(files) {
      this.$root.$emit('close-alert-message');
      const filesArray = Array.from(files);
      if (files.length === 0) {
        return;
      } else {
        filesArray.forEach((file) => {
          if (!file?.type?.includes?.('image/')) {
            return;
          }
          if (file.size > this.maxFileSize) {
            this.$root.$emit('alert-message', this.$t('attachment.tooBigFile.label', {
              0: Number.parseFloat(file.size / 1024 / 1024).toFixed(2).replace('.00', ''),
              1: parseInt(this.maxFileSize / 1024 / 1024),
            }), 'error');
            return;
          }
          if (file?.size) {
            const fileDetails = {
              id: null,
              uploadId: this.generateRandomUploadId(),
              name: file.name,
              size: file.size,
              src: null,
              progress: 0,
              file: file,
              finished: false,
            };
            const self = this;
            const reader = new FileReader();
            reader.onload = (e) => {
              const data = e.target.result;
              this.$set(fileDetails, 'src', data);
              this.$set(fileDetails, 'data', data);
              self.filesArray.push(fileDetails);
              self.$forceUpdate();
            };
            reader.readAsDataURL(file);
            this.$emit('input', fileDetails.uploadId);
            this.uploadFileToServer(file, fileDetails);
          }
        });
        this.$emit('update-images', this.filesArray);
      }
    },
    uploadFileToServer(file, fileDetails) {
      fileDetails.uploadPending = true;
      this.pendingUploads.push(fileDetails);
      Promise.resolve().then(() => this.$uploadService.upload(file, fileDetails.uploadId))
        .then(uploadId => {
          if (!uploadId) {
            throw new Error(UPLOAD_ERROR_KEY);
          }
          fileDetails.uploadAnswered = true;
        })
        .catch(() => this.handleUploadFileError(fileDetails))
        .finally(() => {
          fileDetails.uploadPending = false;
          this.pendingUploads.splice(this.pendingUploads.indexOf(fileDetails), 1);
        });
      this.controlUpload(fileDetails);
    },
    controlUpload(fileDetails) {
      window.setTimeout(() => {
        if (fileDetails.error || fileDetails.removed) {
          return;
        }
        const sentWhileUploadPending = fileDetails.uploadPending;
        const sentAfterUploadAnswered = fileDetails.uploadAnswered;
        this.$uploadService.getUploadProgress(fileDetails.uploadId)
          .then(percent => {
            if (fileDetails.error || fileDetails.removed) {
              return;
            }
            // A poll reads 100 once the body has arrived, before the server
            // has written and validated the file: only a poll sent after the
            // upload request answered completes the image.
            fileDetails.progress = sentAfterUploadAnswered ? Number(percent) : Math.min(Number(percent), 99);
            if (!fileDetails.progress || fileDetails.progress < 100) {
              this.controlUpload(fileDetails);
            }
          })
          .catch(error => {
            if (fileDetails.error || fileDetails.removed) {
              return;
            } else if (sentWhileUploadPending && error?.message !== UPLOAD_FAILED_MESSAGE) {
              // The server knows an upload only once its request reaches it,
              // and a poll sent before then finds none; a late start (a slow
              // network, a browser preparing the body) is the inferred cause of
              // the iPhone uploads that stayed at 0%.
              this.controlUpload(fileDetails);
            } else {
              this.handleUploadFileError(fileDetails);
            }
          });
      }, 200);
    },
    handleUploadFileError(fileDetails) {
      if (fileDetails.error || fileDetails.removed) {
        return;
      }
      fileDetails.error = true;
      this.$emit('delete', fileDetails);
      this.$root.$emit('alert-message', this.$t(UPLOAD_ERROR_KEY), 'error');
    },
    generateRandomUploadId() {
      const random = Math.round(Math.random() * 100000);
      const now = Date.now();
      return `${random}-${now}`;
    },
    handlePasteFiles(event) {
      const files = event?.clipboardData?.file?.length
          && event?.clipboardData?.files
           || event?.clipboardData?.items;
      if (files?.length) {
        const promises = [];
        const imageItems = [];
        for (const file of files) {
          if (file?.type?.includes('image/')) {
            const imageFile = file.getAsFile && file.getAsFile() || file;
            if (imageFile) {
              imageItems.push(imageFile);
            }
          } else {
            const self = this;
            promises.push(
              new Promise(resolve => file.getAsString((s) => resolve.apply(self, [s])))
                .then(filePath => {
                  if (filePath.includes('.png')
                      || filePath.includes('.jpeg')
                      || filePath.includes('.jpg')
                      || filePath.includes('.gif')
                      || filePath.includes('.bmp')) {
                    const imageFile = file.getAsFile && file.getAsFile() || file;
                    if (imageFile) {
                      imageItems.push(imageFile);
                    }
                  }
                })
            );
          }
        }
        Promise.all(promises).then(() => {
          if (imageItems.length) {
            this.uploadFiles(imageItems);
          }
        });
      }
    },
    handleDropFiles(event) {
      const files = event?.detail;
      if (files?.length) {
        const imageItems = [];
        for (const file of files) {
          if (file?.type?.includes('image/')) {
            imageItems.push(file);
          }
        }
        if (imageItems.length) {
          this.uploadFiles(imageItems);
        }
      }
    }
  }
};
</script>