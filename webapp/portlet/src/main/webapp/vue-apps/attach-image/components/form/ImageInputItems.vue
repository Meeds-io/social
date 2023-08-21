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
  <card-carousel v-if="images.length">
    <v-list-item 
      v-for="(image, index) in images" 
      :key="index"
      class="px-2">
      <attachments-image-input-item 
        :image="image"
        :object-id="objectId"
        :object-type="objectType"
        @delete="$emit('delete', $event)" />
    </v-list-item>
    <v-list-item class="px-2">
      <v-btn
        class="border-color grey-background"
        outlined
        width="120"
        min-height="120"
        max-height="120"
        @click="openFileExplorer">
        <v-icon class="primary--text">fa-plus</v-icon>
      </v-btn>
    </v-list-item>
  </card-carousel>
</template>
<script>
export default {
  props: {
    images: {
      type: Array,
      default: () => [],
    },
    objectId: {
      type: String,
      default: null,
    },
    objectType: {
      type: String,
      default: null,
    },
  },
  methods: {
    openFileExplorer() {
      document.dispatchEvent(new CustomEvent('attachments-image-open-file-explorer'));
    },
  }
};
</script>
