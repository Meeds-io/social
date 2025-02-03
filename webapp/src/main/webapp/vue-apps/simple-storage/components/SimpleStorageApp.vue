<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->

<template>
  <v-app>
    <v-card
      class="application-body position-static pb-5"
      flat>
      <h4 class="text-title px-5 pt-5 ma-0">
        {{ $t('simpleStorage.title.label') }}
      </h4>
      <simple-storage-toolbar
        class="mb-3"
        @images-uploaded="handleImagesUploaded" />
      <simple-storage-image-list
        :image-list="images"
        :loading="loading"
        @open-preview="openImagePreview" />
      <v-card-actions class="d-flex">
        <v-btn
          v-if="hasMore"
          :loading="loading"
          class="btn mt-2 px-0 width-full"
          @click="getSavedImages">
          {{ $t('simpleStorage.loadMore.label') }}
        </v-btn>
      </v-card-actions>
    </v-card>
    <attachments-image-preview-dialog
      ref="previewDialog" />
  </v-app>
</template>

<script>

export default {
  data() {
    return {
      images: [],
      objectType: 'public',
      objectId: 'images',
      loading: false,
      pageSize: 9,
      limit: 0,
      offset: 0,
      hasMore: false
    };
  },
  created() {
    this.getSavedImages();
    this.$root.$on('image-attachment-saved', this.handleImageSaved);
  },
  methods: {
    openImagePreview(image) {
      this.$refs.previewDialog.open(this.objectType, this.objectId, this.images, image.id);
    },
    handleImagesUploaded(images) {
      this.images.unshift(...images);
    },
    getSavedImages() {
      this.loading = true;
      this.offset = this.images.length || 0;
      this.limit = this.limit || this.pageSize;
      return this.$fileAttachmentService.getAttachments(this.objectType, this.objectId, this.offset, this.limit + 1).then(data => {
        const newImages = data?.attachments?.map(this.mapImageAttachment) || [];
        this.hasMore = newImages.length > this.limit;
        this.images.push(...newImages);
      }).finally(() => this.loading = false);
    },
    mapImageAttachment(attachment) {
      return {
        id: attachment.id,
        thumbnail: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/${this.objectType}/${this.objectId}/${attachment.id}?size=60x26`,
        name: attachment.name,
        creationDate: attachment.updated,
        size: attachment.size,
      };
    },
    handleImageSaved(image, uploadId) {
      const existingImage = this.images.find(img => img.uploadId === uploadId);
      if (existingImage) {
        Object.assign(existingImage, this.mapImageAttachment(image));
      }
    },
  }
};
</script>
