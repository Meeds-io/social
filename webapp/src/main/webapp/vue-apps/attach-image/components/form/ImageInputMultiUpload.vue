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
    id="attachedImagesField"
    ref="attachedImages"
    accept="image/*"
    class="hidden"
    clearable
    multiple
    @change="uploadFiles" />
</template>
<script>
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
    filesArray: []
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
    reset() {
      this.filesArray = [];
    },
    deleteFile(uploadId) {
      const fileIndex = this.filesArray.findIndex(f => f.uploadId === uploadId);
      this.filesArray.splice(fileIndex, 1);
      this.$uploadService.deleteUpload(uploadId);
    },
    triggerFileClickEvent() {
      document.getElementById('attachedImagesField').click();
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
      try {
        this.$uploadService.upload(file, fileDetails.uploadId)
          .catch(error => this.handleUploadFileError(fileDetails, String(error)));
        this.controlUpload(fileDetails);
      } catch (e) {
        this.handleUploadFileError(fileDetails, String(e));
      }
    },
    controlUpload(fileDetails) {
      window.setTimeout(() => {
        this.$uploadService.getUploadProgress(fileDetails.uploadId)
          .then(percent => {
            fileDetails.progress = Number(percent);
            if (!fileDetails.error && (!fileDetails.progress || fileDetails.progress < 100)) {
              this.controlUpload(fileDetails);
            }
          });
      }, 200);
    },
    handleUploadFileError(fileDetails, errorKey) {
      fileDetails.error = true;
      this.$emit('delete', fileDetails);
      this.$root.$emit('alert-message', this.$t(errorKey), 'error');
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