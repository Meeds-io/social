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
  <div class="pt-4">
    <div class="d-flex pb-2">
      <div>
        <slot name="title"></slot>
      </div>
      <v-spacer />
      <v-tooltip :disabled="disableTooltip" bottom>
        <template #activator="{on, attrs}">
          <div
            v-on="on"
            v-bind="attrs">
            <v-btn
              v-if="value || sendingImage"
              id="deleteImageFileInput"
              :loading="sendingImage"
              :aria-label="$t('layout.deleteBackgroundImageTitle')"
              icon
              dense
              @click="reset">
              <v-icon color="error" dense>fa-trash</v-icon>
            </v-btn>
            <v-file-input
              v-else
              id="imageFileInput"
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
        <span>{{ value && $t('layout.deleteBackgroundImageTitle') || $t('layout.uploadBackgroundImageTitle') }}</span>
      </v-tooltip>
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
    storageId: {
      type: String,
      default: null,
    },
    immediateSave: {
      type: Boolean,
      default: false,
    },
    objectType: {
      type: String,
      default: 'containerBackground',
    },
  },
  data: () => ({
    changed: false,
    sendingImage: false,
    uploadId: null,
    disableTooltip: false,
    attachments: null,
  }),
  computed: {
    illustrationId() {
      return this.attachments?.[0]?.id;
    },
    remoteIllustrationSrc() {
      return this.illustrationId && `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/${this.objectType}/${this.storageId}/${this.illustrationId}` || null;
    },
  },
  watch: {
    sendingImage() {
      this.$emit('sending', this.sendingImage);
    },
    value() {
      this.disableTooltip = true;
      window.setTimeout(() => this.disableTooltip = false, 50);
    },
  },
  methods: {
    uploadFile(file) {
      if (file && file.size) {
        if (file.size > this.maxUploadSize) {
          this.$root.$emit('alert-message', this.$t(this.$uploadService.avatarExcceedsLimitError), 'error');
          return;
        }
        this.sendingImage = true;
        const thiss = this;
        return this.$uploadService.upload(file)
          .then(uploadId => {
            const reader = new FileReader();
            reader.onload = (e) => {
              if (thiss.immediateSave) {
                thiss.save();
              } else {
                thiss.$emit('input', e.target.result);
                thiss.$forceUpdate();
              }
            };
            reader.readAsDataURL(file);
            this.uploadId = uploadId;
            this.changed = true;
          })
          .then(() => this.$emit('refresh'))
          .catch(() => this.$root.$emit('alert-message', this.$t('layout.errorUploadingPreview'), 'error'))
          .finally(() => this.sendingImage = false);
      }
    },
    reset() {
      this.uploadId = null;
      this.$emit('input', null);
      this.changed = true;
    },
    save() {
      if (this.changed) {
        return this.$fileAttachmentService.saveAttachments({
          objectType: this.objectType,
          objectId: this.storageId,
          uploadedFiles: this.uploadId && [{uploadId: this.uploadId}] || [],
          attachedFiles: [],
        }).then((report) => {
          if (report?.errorByUploadId?.length) {
            const attachmentHtmlError = Object.values(report.errorByUploadId).join('<br>');
            this.$root.$emit('alert-message-html', attachmentHtmlError, 'error');
          } else {
            return this.getAttachments()
              .then(() => {
                if (this.$refs.uploadInput) {
                  this.$refs.uploadInput.reset();
                }
                if (this.immediateSave) {
                  this.$emit('input', this.remoteIllustrationSrc);
                }
                return this.remoteIllustrationSrc;
              });
          }
        });
      }
    },
    async getAttachments() {
      return await this.$fileAttachmentService.getAttachments(this.objectType, this.storageId)
        .then(data => this.attachments = data?.attachments || []);
    },
  },
};
</script>