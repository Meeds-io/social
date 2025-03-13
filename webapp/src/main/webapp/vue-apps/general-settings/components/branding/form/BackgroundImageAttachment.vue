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
    <slot name="title"></slot>
    <v-spacer />
    <v-tooltip
      bottom
      :disabled="disableTooltip">
      <template #activator="{on, attrs}">
        <div
          v-bind="attrs"
          v-on="on">
          <v-btn
            v-if="hasFile"
            id="deleteImageFileInput"
            :aria-label="$t('generalSettings.deleteBackgroundImageTitle')"
            dense
            icon
            @click="reset">
            <v-icon
              color="error"
              dense>
              fa-trash
            </v-icon>
          </v-btn>
          <v-file-input
            v-else
            id="pageBackgroundImageFileInput"
            ref="uploadInput"
            accept="image/*"
            class="file-selector pa-0 ma-0"
            clearable
            dense
            :loading="sendingImage"
            prepend-icon="fas fa-camera z-index-two rounded-circle primary-border-color white py-1 ms-3"
            rounded
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
      disableTooltip: false,
      uploadId: null,
    }),
    watch: {
      uploadId () {
        this.$emit('input', this.uploadId);
      },
      hasFile () {
        this.disableTooltip = true;
        window.setTimeout(() => this.disableTooltip = false, 50);
      },
    },
    methods: {
      uploadFile (file) {
        if (file?.size) {
          this.sendingImage = true;
          const thiss = this;
          return eXo.$uploadService.upload(file)
            .then(uploadId => {
              return new Promise(resolve => {
                const reader = new FileReader();
                reader.onload = e => {
                  thiss.$emit('image-data-updated', e?.target?.result);
                  resolve(uploadId);
                };
                reader.readAsDataURL(file);
              });
            })
            .then(uploadId => {
              this.changed = true;
              this.uploadId = uploadId;
              this.sendingImage = false;
              this.$emit('refresh');
            })
            .catch(() => {
              this.$root.$emit('alert-message', this.$t('generalSettings.errorUploadingPreview'), 'error');
              this.sendingImage = false;
            });
        }
      },
      reset () {
        this.uploadId = 0;
        this.$emit('input', 0);
        this.$emit('reset');
        this.changed = true;
      },
    },
  };
</script>