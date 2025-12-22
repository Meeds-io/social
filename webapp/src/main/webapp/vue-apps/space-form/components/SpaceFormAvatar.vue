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
  <div>
    <div class="d-flex position-relative my-4">
      <div class="flex-grow-0 text-header">
        {{ $t('spacesList.label.avatarLabel') }}
      </div>
      <div class="flex-grow-1 ms-2 position-relative">
        <div class="absolute-vertical-center">
          <v-btn
            ref="avatarInput"
            id="spaceAvatarEditButton"
            :title="$t('spacesList.label.changeAvatar')"
            icon
            outlined
            @click="$refs.imageCropDrawer.open()">
            <v-icon size="18">fa-camera</v-icon>
          </v-btn>
          <v-btn
            v-show="!isDefaultAvatar"
            :title="$t('spacesList.label.deleteAvatar')"
            id="spaceAvatarDeleteButton"
            outlined
            icon
            @click="removeAvatar">
            <v-icon size="18">fa-undo</v-icon>
          </v-btn>
        </div>
      </div>
    </div>
    <img
      v-if="imageData"
      :src="imageData"
      width="50px"
      height="50px"
      class="border-radius clickable"
      @click="$refs.imageCropDrawer.open()">
    <v-avatar
      v-else
      class="clickable"
      color="primary"
      width="50px"
      height="50px"
      rounded
      @click="$refs.imageCropDrawer.open()">
      <span class="white--text text-h5">{{ nameInitials }}</span>
    </v-avatar>
    <image-crop-drawer
      ref="imageCropDrawer"
      :crop-options="cropOptions"
      :max-file-size="maxUploadSizeInBytes"
      :src="imageData"
      max-image-width="1280"
      drawer-title="UIChangeAvatarContainer.label.ChangeAvatar"
      @apply="updateImage" />
  </div>
</template>
<script>
export default {
  props: {
    maxUploadSize: {
      type: Number,
      default: () => 2,
    },
    name: {
      type: String,
      default: null,
    },
    src: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    imageData: null,
    cropOptions: {
      aspectRatio: 1,
      viewMode: 1,
    },
  }),
  watch: {
    imageData(newVal) {
      this.$emit('avatar-updated', newVal);
    }
  },
  computed: {
    isDefaultAvatar() {
      return this.src || !this.imageData;
    },
    nameInitials() {
      if (this.name) {
        return this.name.split(' ').filter(n => n?.length).map(n => n.at(0).toUpperCase()).slice(0, 2).join('');
      } else {
        return '';
      }
    },
    maxUploadSizeInBytes() {
      return this.maxUploadSize * 1024 * 1024;
    },
  },
  created() {
    this.imageData = this.src;
  },
  methods: {
    removeAvatar() {
      this.imageData = null;
      this.$emit('input', null);
    },
    updateAvatar(uploadId) {
      this.$emit('input', uploadId);
    },
    updateImage(image) {
      this.imageData = image.src;
      this.updateAvatar(image.uploadId);
    }
  },
};
</script>