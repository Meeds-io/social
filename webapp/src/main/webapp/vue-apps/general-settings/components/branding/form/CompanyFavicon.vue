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
  <v-hover>
    <v-img
      slot-scope="{ hover }"
      :src="faviconPreviewSrc" 
      role="presentation"
      transition="none"
      width="50"
      min-height="50"
      max-height="50"
      contain
      eager>
      <v-expand-transition>
        <div
          v-show="hover"
          class="justify-center light-grey-background absolute-full-size">
          <v-btn
            v-if="hover && !isDefaultSrc"
            color="error"
            class="absolute-all-center z-index-two white error"
            icon
            border
            outlined
            @click="resetFavicon">
            <v-icon>fas fa-trash</v-icon>
          </v-btn>
          <v-file-input
            v-if="!resetInput"
            v-show="isDefaultSrc"
            id="faviconFileInput"
            ref="faviconFileInput"
            :loading="sendingImage"
            prepend-icon="fas fa-camera z-index-two rounded-circle primary-border-color primary--text white py-1 absolute-all-center"
            accept="image/*"
            class="file-selector"
            rounded
            clearable
            @change="uploadFile" />
        </div>
      </v-expand-transition>
    </v-img>
  </v-hover>
</template>

<script>
export default {
  props: {
    value: {
      type: String,
      default: () => null,
    },
    branding: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    sendingImage: false,
    faviconSrc: null,
    uploadInProgress: false,
    uploadProgress: 0,
    maxFileSize: 10240,
    resetInput: false,
  }),
  computed: {
    isDefaultSrc() {
      return !this.faviconSrc;
    },
    faviconPreviewSrc() {
      if (this.faviconSrc) {
        return this.$utils.convertImageDataAsSrc(this.faviconSrc);
      } else {
        return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/platform/branding/favicon?v=${Math.random()}`;
      }
    },
  },
  watch: {
    sendingImage() {
      if (this.sendingImage) {
        document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      } else {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      }
    },
  },
  methods: {
    resetFavicon() {
      this.faviconSrc = null;
      this.$emit('input', null);
      this.sendingImage = false;
      if (this.$refs.faviconFileInput) {
        this.resetInput = true;
        this.$nextTick().then(() => this.resetInput = false);
      }
    },
    uploadFile(file) {
      this.$root.$emit('close-alert-message');
      if (file && file.size) {
        if (file.type && file.type.indexOf('image/') !== 0) {
          this.$root.$emit('alert-message', this.$t('generalSettings.mustImage.label'), 'error');
          return;
        }
        if (file.size > this.maxFileSize) {
          this.$root.$emit('alert-message', this.$t('generalSettings.favicon.tooBigFile.label'), 'error');
          return;
        }
        this.sendingImage = true;
        const self = this;
        return this.$uploadService.upload(file)
          .then(uploadId => {
            if (uploadId) {
              const reader = new FileReader();
              reader.onload = (e) => {
                self.faviconSrc = e.target.result;
                self.$forceUpdate();
              };
              reader.readAsDataURL(file);
              this.$emit('input', uploadId);
            } else {
              this.$root.$emit('alert-message', this.$t('generalSettings.uploadingError'), 'error');
            }
          })
          .catch(error => this.$root.$emit('alert-message', this.$t(String(error)), 'error'))
          .finally(() => this.sendingImage = false);
      }
    },
  },
};
</script>