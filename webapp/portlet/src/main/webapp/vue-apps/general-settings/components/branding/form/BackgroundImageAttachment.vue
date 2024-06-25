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
  <div class="d-flex">
    <div class="text-subtitle-1">
      <slot name="title"></slot>
    </div>
    <v-spacer />
    <v-tooltip bottom>
      <template #activator="{on, attrs}">
        <div
          v-on="on"
          v-bind="attrs">
          <v-btn
            v-if="hasFile"
            id="deleteImageFileInput"
            :aria-label="$t('generalSettings.deleteBackgroundImageTitle')"
            icon
            dense
            @click="reset">
            <v-icon color="error" dense>fa-trash</v-icon>
          </v-btn>
          <v-file-input
            v-else
            id="pageBackgroundImageFileInput"
            :loading="sendingImage"
            ref="uploadInput"
            accept="image/*"
            prepend-icon="fas fa-camera z-index-two rounded-circle primary-border-color white py-1 ms-3"
            class="file-selector pa-0 ma-0"
            rounded
            clearable
            dense
            @change="uploadFile" />
        </div>
      </template>
      <span>{{ value && $t('generalSettings.deleteBackgroundImageTitle') || $t('generalSettings.uploadBackgroundImageTitle') }}</span>
    </v-tooltip>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
    hasFile: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    changed: false,
    sendingImage: false,
    uploadId: null,
  }),
  watch: {
    uploadId() {
      this.$emit('input', this.uploadId);
    },
  },
  methods: {
    uploadFile(file) {
      if (file?.size) {
        this.sendingImage = true;
        const thiss = this;
        return this.$uploadService.upload(file)
          .then(uploadId => {
            this.uploadId = uploadId;
            const reader = new FileReader();
            reader.onload = e => thiss.$emit('image-data-updated', e?.target?.result);
            reader.readAsDataURL(file);

            this.changed = true;
            this.sendingImage = false;
          })
          .then(() => this.$emit('refresh'))
          .catch(() => {
            this.$root.$emit('alert-message', this.$t('generalSettings.errorUploadingPreview'), 'error');
            this.sendingImage = false;
          });
      }
    },
    reset() {
      this.uploadId = null;
      this.$emit('input', 0);
      this.changed = true;
    },
  },
};
</script>