<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <div class="d-flex flex-nowrap align-center">
    <portal-general-settings-navigation-settings-icon
      :icon-size="iconSize"
      :icon="icon || iconUrl"
      class="flex-grow-0 flex-shrink-0 me-4" />
    <font-icon-input
      v-model="icon"
      class="my-auto me-4"
      button-label="generalSettings.sidebar.choose"
      no-default
      no-label
      no-icon />
    <div class="position-relative overflow-hidden">
      <v-file-input
        v-if="!resetInput"
        id="iconFileInput"
        ref="iconFileInput"
        accept="image/*"
        class="position-absolute t-0 l-0 full-width pa-0 ma-0"
        prepend-icon=""
        hide-details
        hide-input
        @change="uploadFile" />
      <v-btn
        :loading="sending"
        class="position-relative z-index-two btn primary"
        border
        outlined
        @click="openFileUpload">
        {{ $t('generalSettings.sidebar.upload') }}
      </v-btn>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
    siteId: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    iconSize: 34,
    maxFileSize: 102400,
    sending: false,
    resetInput: false,
  }),
  computed: {
    iconUrl: {
      get: function() {
        if (this.value?.includes?.('base64') || this.value?.includes?.('/')) {
          return this.value;
        } else {
          return null;
        }
      },
      set: function(newValue) {
        if (newValue) {
          this.$emit('input', newValue);
        }
      }
    },
    icon: {
      get: function() {
        if (this.iconUrl) {
          return null;
        } else {
          return this.value;
        }
      },
      set: function(newValue) {
        if (newValue) {
          this.uploadId = null;
          this.$emit('input', newValue);
        }
      }
    },
  },
  methods: {
    async save() {
      if (this.uploadId) {
        const fileAttachment = await this.$fileAttachmentService.createAttachment({
          objectType: 'site',
          objectId: this.siteId,
          fileAttachmentObject: {
            uploadId: this.uploadId,
          },
        });
        return `/portal/rest/v1/social/attachments/site/${this.siteId}/${fileAttachment.id}?lastModified=${fileAttachment.updated}`;
      } else {
        return this.value;
      }
    },
    openFileUpload() {
      this.$refs.iconFileInput.$el.querySelector('input').click();
    },
    async uploadFile(file) {
      this.$root.$emit('close-alert-message');
      if (file?.size) {
        if (file.size > this.maxFileSize) {
          this.$root.$emit('alert-message', this.$t('generalSettings.navigationIcon.tooBigFile'), 'error');
          return;
        }
        try {
          this.uploadId = await this.$uploadService.upload(file);
          const reader = new FileReader();
          const self = this;
          reader.onload = (e) => {
            self.iconUrl = self.$utils.convertImageDataAsSrc(e.target.result);
          };
          reader.readAsDataURL(file);
        } catch (error) {
          this.$root.$emit('alert-message', this.$t(String(error)), 'error');
        } finally {
          this.sending = false;
        }
      }
    },
  },
};
</script>