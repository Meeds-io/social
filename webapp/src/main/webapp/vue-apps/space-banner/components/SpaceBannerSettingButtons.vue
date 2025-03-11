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
  <div
    v-show="hover"
    class="d-flex flex-grow-1 position-absolute full-height full-width">
    <div class="me-2 ms-auto my-auto mt-sm-2 mb-sm-0">
      <v-btn
        v-show="!defaultBanner && hover"
        id="spaceBannerDeleteButton"
        class="changeBannerButton mask-color border-color"
        dark
        icon
        outlined
        :title="$t('UIPopupBannerUploader.title.deleteBanner')"
        @click="$emit('remove')">
        <v-icon
          color="error"
          size="18">
          mdi-delete
        </v-icon>
      </v-btn>
      <v-btn
        v-show="hover"
        id="spaceBannerEditButton"
        ref="bannerInput"
        class="changeBannerButton mask-color border-color"
        dark
        icon
        outlined
        @click="$emit('edit')">
        <v-icon size="18">
          fas fa-file-image
        </v-icon>
      </v-btn>
    </div>
  </div>
</template>
<script>
  export default {
    props: {
      hover: {
        type: Boolean,
        default: false,
      },
      defaultBanner: {
        type: Boolean,
        default: false,
      },
    },
  };
</script>