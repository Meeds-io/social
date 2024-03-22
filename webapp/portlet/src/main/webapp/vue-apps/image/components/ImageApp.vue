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
  <v-app ref="app" class="overflow-hidden position-relative">
    <v-hover v-model="hover" :disabled="!canEdit">
      <v-card
        :color="$root.hasImages && 'transparent' || 'primary'"
        class="card-border-radius"
        min-width="100%"
        flat>
        <v-responsive :aspect-ratio="$root.imageAspectRatio">
          <image-settings-header
            v-if="canEdit"
            :hover="hover || loading"
            :loading="loading"
            @edit="editSettings"
            @remove="removeImage" />
          <image-view />
        </v-responsive>
      </v-card>
    </v-hover>
    <template v-if="canEdit">
      <image-settings-drawer
        v-if="edit"
        ref="drawer"
        @loading="loading = $event"
        @update="updateImage" />
      <exo-confirm-dialog
        v-if="removeConfirm"
        ref="confirmDialog"
        :title="$t('image.label.removeImageConfirmTitle')"
        :message="$t('image.label.removeImageConfirmMessage')"
        :ok-label="$t('image.label.yes')"
        :cancel-label="$t('image.label.no')"
        @ok="removeImageConfirm"
        @dialog-closed="removeConfirm = false" />
    </template>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    hover: false,
    loading: false,
    saving: false,
    edit: false,
    removeConfirm: false,
    previewMode: false,
  }),
  computed: {
    width() {
      return this.$refs?.app?.$el?.offsetWidth;
    },
    height() {
      return this.$refs?.app?.$el?.offsetHeight;
    },
    canEdit() {
      return !this.previewMode && this.$root.canEdit;
    },
  },
  created() {
    document.addEventListener('cms-preview-mode', this.switchToPreview);
    document.addEventListener('cms-edit-mode', this.switchToEdit);
  },
  methods: {
    editSettings() {
      this.edit = true;
      this.$nextTick().then(() => {
        window.setTimeout(() => this.$refs?.drawer?.open?.(), 50);
      });
    },
    removeImage() {
      this.removeConfirm = true;
      this.$nextTick().then(() => {
        window.setTimeout(() => this.$refs?.confirmDialog?.open?.(), 50);
      });
    },
    removeImageConfirm() {
      this.loading = true;
      return this.$fileAttachmentService.saveAttachments({
        objectType: this.$root.objectType,
        objectId: this.$root.name,
      })
        .then(() => this.$root.files = null)
        .finally(() => this.loading = false);
    },
    updateImage(imageItem) {
      this.loading = true;
      return this.$fileAttachmentService.saveAttachments({
        objectType: this.$root.objectType,
        objectId: this.$root.name,
        uploadedFiles: [{
          uploadId: imageItem.uploadId,
          altText: imageItem.altText,
          format: imageItem.format,
        }],
      })
        .then(() => this.refresh())
        .finally(() => this.loading = false);
    },
    refresh() {
      this.loading = true;
      return this.$fileAttachmentService.getAttachments(this.$root.objectType, this.$root.name)
        .then(data => this.$root.files = data?.attachments || [])
        .finally(() => this.loading = false);
    },
    switchToPreview() {
      this.previewMode = true;
    },
    switchToEdit() {
      this.previewMode = false;
    },
  },
};
</script>