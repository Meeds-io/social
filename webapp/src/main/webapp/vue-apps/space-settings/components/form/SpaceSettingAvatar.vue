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
    <div class="text-header d-flex position-relative my-4">
      <div class="flex-grow-0">
        {{ $t('SpaceSettings.label.avatar') }}
      </div>
      <div class="flex-grow-1 ms-2 position-relative">
        <div class="absolute-vertical-center">
          <v-btn
            id="spaceAvatarEditButton"
            ref="avatarInput"
            icon
            outlined
            @click="$refs.imageCropDrawer.open()">
            <v-icon size="18">
              fa-camera
            </v-icon>
          </v-btn>
        </div>
      </div>
    </div>
    <v-img
      id="spaceAvatarImg"
      class="d-flex border-radius"
      :class="!avatarUrl && 'primary'"
      contain
      eager
      height="45"
      :lazy-src="imageData || avatarUrl || ''"
      :src="imageData || avatarUrl || ''"
      width="45">
      <v-card
        class="full-width full-height"
        flat
        @click="$refs.imageCropDrawer.open()" />
    </v-img>
    <image-crop-drawer
      ref="imageCropDrawer"
      :crop-options="cropOptions"
      drawer-title="UIChangeAvatarContainer.label.ChangeAvatar"
      :max-file-size="maxUploadSizeInBytes"
      max-image-width="1280"
      :src="imageData || `${avatarUrl}&size=0`"
      @data="imageData = $event"
      @input="$emit('input', $event)" />
  </div>
</template>
<script>
  export default {
    props: {
      maxUploadSize: {
        type: Number,
        default: () => 2,
      },
    },
    data: () => ({
      imageData: null,
      cropOptions: {
        aspectRatio: 1,
        viewMode: 1,
      },
    }),
    computed: {
      avatarUrl () {
        return this.$root.space?.avatarUrl;
      },
      maxUploadSizeInBytes () {
        return this.maxUploadSize * 1024 * 1024;
      },
      height () {
        if (this.$root.isMobile) {
          return 125;
        } else {
          return 175;
        }
      },
    },
  };
</script>