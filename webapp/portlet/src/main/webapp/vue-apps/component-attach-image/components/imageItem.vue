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
      {{ imageUploadProgress }}
    </v-progress-circular>
  </v-card> 
  <v-card 
    v-else 
    class="position-relative" 
    flat>
    <v-img
      :src="imageItem"
      class="white--text align-end"
      gradient="to bottom, rgba(0,0,0,.1), rgba(0,0,0,.5)"
      width="120"
      min-height="120"
      max-height="120" />
    <v-card-actions class="position-absolute t-0 r-0">
      <v-btn 
        class="ml-0" 
        icon 
        small
        @click="deleteFile()">
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
    }
  },
  computed: {
    imageItem() {
      return this.image && this.image.src;
    },
    imageUploadProgress() {
      return this.image && this.image.progress;
    },
  },
  methods: {
    deleteFile() {
      this.$root.$emit('delete-file', this.image.uploadId);
    }
  }
};
</script>
