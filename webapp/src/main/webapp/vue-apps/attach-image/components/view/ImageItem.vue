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
  <v-card
    :class="{ 'border-color': !hover }"
    :loading="loading"
    :width="previewWidth"
    :elevation="hover ? 4 : 0"
    class="attachment-card-item overflow-hidden d-flex flex-column border-box-sizing mx-2"
    height="210px"
    max-height="210px"
    max-width="250px"
    @click="$emit('preview-attachment')"
    @focus="hover = true"
    @blur="hover = false"
    @mouseenter="hover = true"
    @mouseleave="hover = false">
    <v-card-text class="attachment-card-item-thumbnail d-flex flex-grow-1 pa-0">
      <img
        :src="thumbnailUrl"
        :alt="attachmentAlt"
        class="ma-auto full-width">
    </v-card-text>
    <div v-if="isGifImage" class="position-absolute white border-radius r-3 mt-2">
      <v-chip
        outlined
        label
        small>
        GIF
      </v-chip>
    </div>
    <v-expand-transition>
      <v-card
        v-if="invalid"
        class="d-flex flex-column transition-fast-in-fast-out v-card--reveal"
        elevation="0"
        style="height: 100%;">
        <v-card-text class="pb-0 d-flex flex-row">
          <v-icon color="error">fa-exclamation-circle</v-icon>
          <p class="my-auto ms-2 font-weight-bold">
            {{ $t('attachments.errorAccessingFile') }}
          </p>
        </v-card-text>
        <v-card-text class="flex-grow-1">
          <p>{{ $t('attachments.alert.unableToAccessFile') }}</p>
        </v-card-text>
        <v-card-actions class="pt-0">
          <v-btn
            text
            color="primary"
            @click="closeErrorBox">
            {{ $t('attachments.close') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-expand-transition>
  </v-card>
</template>

<script>
export default {
  props: {
    attachment: {
      type: Object,
      default: null,
    },
    objectType: {
      type: String,
      default: null,
    },
    objectId: {
      type: String,
      default: null,
    },
    previewHeight: {
      type: Number,
      default: () => 50,
    },
    previewWidth: {
      type: Number,
      default: () => 50,
    },
  },
  computed: {
    thumbnailUrl() {
      return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/${this.objectType}/${this.objectId}/${this.attachment.id}?lastModified=${this.attachment.updated}&size=${this.attachmentPreveiwHeight}x${this.attachmentPreveiwWidth}`;
    },
    attachmentAlt() {
      return this.attachment?.alt || 'attached image';
    },
    isGifImage() {
      return this.attachment?.mimetype === 'image/gif';
    },
    isSmallImage() {
      return this.attachment?.size < 1000000;
    },
    attachmentPreveiwHeight() {
      return this.defaultSize || this.isSmallImage && !this.isGifImage ? 0 : this.previewHeight;
    },
    attachmentPreveiwWidth() {
      return this.defaultSize || this.isSmallImage && !this.isGifImage ? 0 : this.previewHeight;
    },
  },
  watch: {
    hover() {
      if (this.hover && this.isGifImage) {
        this.defaultSize = true;
      } else {
        this.defaultSize = false;
      }
    }
  },
  data: () => ({
    loading: false,
    invalid: false,
    hover: false,
    defaultSize: false,
  }),
  methods: {
    closeErrorBox(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      window.setTimeout(() => {
        this.invalid = false;
      }, 50);
    },
  },
};
</script>
