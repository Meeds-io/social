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
  <v-app>
    <input 
      id="attachedImagesField"
      class="hidden"
      ref="attachedImages"
      type="file" 
      name="attachedImages" 
      accept="image/*"
      multiple
      @change="uploadFiles($refs.attachedImages.files)">
  </v-app>
</template>
<script>
export default {
  data: () => ({
    filesArray: [],
    maxFileSize: 20097152
  }),
  created() {
    document.addEventListener('open-file-explorer', () => {
      this.triggerFileClickEvent();
    });
    this.$root.$on('delete-file', this.removeUplodedFile);
  },
  methods: {
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
          if (file.type && file.type.indexOf('image/') !== 0) {
            this.$root.$emit('alert-message', this.$t('generalSettings.mustImage.label'), 'error');
            return;
          }
          if (file.size > this.maxFileSize) {
            this.$root.$emit('alert-message', this.$t('generalSettings.logo.tooBigFile.label'), 'error');
            return;
          }
          if (file && file.size) {
            if (file.type && file.type.indexOf('image/') !== 0) {
              this.$root.$emit('alert-message', this.$t('imageCropDrawer.mustImage.label'), 'error');
              return;
            }
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
            this.$uploadService.upload(file, fileDetails.uploadId)
              .catch(error => this.$root.$emit('alert-message', this.$t(String(error)), 'error'));
            this.controlUpload(fileDetails);
          } 
        });
        this.$emit('update-images', this.filesArray);
      }
    },
    uploadFileToServer(file) {
      this.$uploadService.upload(file, file.uploadId);
      this.controlUpload(file);
    },
    controlUpload(file) {
      window.setTimeout(() => {
        this.$uploadService.getUploadProgress(file.uploadId)
          .then(percent => {
            file.progress = Number(percent);
            if (!file.progress || file.progress < 100) {
              this.controlUpload(file);
            }
          });
      }, 200);
    },
    generateRandomUploadId() {
      const random = Math.round(Math.random() * 100000);
      const now = Date.now();
      return `${random}-${now}`;
    },
    removeUplodedFile(uploadId) {
      if (uploadId) {
        const fileIndex = this.filesArray.findIndex(f => f.uploadId === uploadId);
        this.filesArray.splice(fileIndex, 1);
        this.$emit('update-images', this.filesArray);
      }
      this.$uploadService.deleteUpload(uploadId);
    }
  }
};
</script>