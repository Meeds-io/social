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
    v-if="imageUploadProgress < 100"
    class="d-flex align-center justify-center grey-background"
    width="120"
    min-height="120"
    max-height="120"
    flat> 
    <v-progress-circular
      :rotate="-90"
      :size="40"
      :width="4"
      :value="imageUploadProgress"
      color="primary">
      {{ imageUploadProgress }}%
    </v-progress-circular>
    <v-card-actions class="position-absolute t-0 r-0">
      <v-btn
        class="ml-0"
        fab
        x-small
        @click="deleteFile">
        <v-icon class="error-color" small>fa-trash</v-icon>
      </v-btn>
    </v-card-actions>
  </v-card> 
  <v-card 
    v-else 
    class="position-relative" 
    flat>
    <v-img
      :src="imageItem.src"
      class="white--text align-end"
      width="120"
      min-height="120"
      max-height="120" />
    <v-card-actions class="position-absolute t-0 r-0">
      <v-btn 
        class="mr-1" 
        fab 
        x-small
        @click="openImageCropDrawer">
        <v-icon class="icon-default-color" small>fa-edit</v-icon>
      </v-btn>
      <v-btn 
        class="ml-0" 
        fab 
        x-small
        @click.prevent.stop="deleteFile">
        <v-icon class="error-color" small>fa-trash</v-icon>
      </v-btn>
    </v-card-actions>
  </v-card>
</template>
<script>
export default {
  props: {
    image: {
      type: String,
      default: ''
    },
    objectId: {
      type: String,
      default: null
    },
    objectType: {
      type: String,
      default: null
    },
  },
  computed: {
    imageItem() {
      return this.image;
    },
    imageUploadProgress() {
      return this.image?.progress;
    },
    fileId() {
      return !this.image?.uploadId ? this.image?.name : '';
    }
  },
  methods: {
    deleteFile() {
      this.$emit('delete', this.image);
    },
    openImageCropDrawer() {
      if (this.imageItem?.src) {
        this.imageItem.src = this.imageItem.src.split('&')[0];
      }
      document.dispatchEvent(new CustomEvent('attachments-image-open-crop-drawer',{detail: this.imageItem}));
    },
  }
};
</script>
