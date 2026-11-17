<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
  <div class="brandingImageTile d-flex flex-column align-center">
    <div class="text-subtitle mb-2">
      {{ label }}
    </div>
    <v-img
      :src="src"
      role="presentation"
      transition="none"
      class="brandingImageTilePicture border-color border-radius light-grey-background"
      width="120"
      height="70"
      contain
      eager />
    <div v-if="help" class="text-subtitle text-center mt-1">
      {{ help }}
    </div>
    <div class="d-flex mt-2">
      <v-btn
        :loading="sending"
        class="brandingImageTileUpload btn"
        small
        @click="$refs.fileInput.click()">
        <span class="text-none">
          {{ src ? $t('generalSettings.branding.tile.replace') : $t('generalSettings.branding.tile.upload') }}
        </span>
      </v-btn>
      <v-btn
        v-if="src"
        class="brandingImageTileRemove btn ms-2"
        small
        @click="$emit('remove')">
        <span class="text-none">
          {{ $t('generalSettings.branding.tile.remove') }}
        </span>
      </v-btn>
    </div>
    <input
      :id="inputId"
      ref="fileInput"
      :accept="accept"
      :aria-label="label"
      type="file"
      class="d-none"
      @change="select">
  </div>
</template>
<script>
export default {
  props: {
    label: {
      type: String,
      default: null,
    },
    // the file input's id, named after the image it uploads
    inputId: {
      type: String,
      default: null,
    },
    src: {
      type: String,
      default: null,
    },
    help: {
      type: String,
      default: null,
    },
    accept: {
      type: String,
      default: 'image/*',
    },
    // maximum size in bytes of an image
    maxSize: {
      type: Number,
      default: 1048576,
    },
    sizeErrorKey: {
      type: String,
      default: 'generalSettings.branding.logo.tooBig',
    },
  },
  data: () => ({
    sending: false,
  }),
  methods: {
    select(event) {
      const file = event.target.files?.[0];
      event.target.value = null;
      if (!file?.size) {
        return;
      }
      this.$root.$emit('close-alert-message');
      if (file.type && file.type.indexOf('image/') !== 0) {
        this.$root.$emit('alert-message', this.$t('generalSettings.mustImage.label'), 'error');
      } else if (file.size > this.maxSize) {
        this.$root.$emit('alert-message', this.$t(this.sizeErrorKey), 'error');
      } else {
        this.upload(file);
      }
    },
    upload(file) {
      this.sending = true;
      return this.$uploadService.upload(file)
        .then(uploadId => {
          if (!uploadId) {
            throw new Error('generalSettings.uploadingError');
          }
          return new Promise(resolve => {
            const reader = new FileReader();
            reader.onload = e => resolve(this.$emit('upload', uploadId, e.target.result));
            reader.readAsDataURL(file);
          });
        })
        .catch(error => this.$root.$emit('alert-message', this.$te(String(error.message)) && this.$t(error.message) || this.$t('generalSettings.uploadingError'), 'error'))
        .finally(() => this.sending = false);
    },
  },
};
</script>
