<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
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
  <v-file-input
    v-if="!sendingImage"
    v-show="skeleton || hover"
    ref="bannerInput"
    :class="skeleton && 'skeleton-background skeleton-text' || ''"
    prepend-icon="fa-file-image"
    class="changeBannerButton mr-4"
    accept="image/*"
    clearable
    @change="uploadBanner" />
</template>

<script>
export default {
  props: {
    maxUploadSize: {
      type: Number,
      default: () => 2,
    },
    skeleton: {
      type: Boolean,
      default: () => true,
    },
    hover: {
      type: Boolean,
      default: () => false,
    },
  },
  data: () => ({
    sendingImage: false,
  }),
  watch: {
    sendingImage() {
      if (this.sendingImage) {
        document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      } else {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      }
    },
  },
  methods: {
    uploadBanner(file) {
      if (file && file.size) {
        if (file.type && file.type.indexOf('image/') !== 0) {
          this.$emit('error', this.$t('profile.warning.message.fileType'));
          return;
        }
        if (file.size > this.maxUploadSize) {
          this.$emit('error', this.$uploadService.bannerExcceedsLimitError);
          return;
        }
        this.sendingImage = true;
        return this.$uploadService.upload(file)
          .then(uploadId => this.$userService.updateProfileField(eXo.env.portal.userName, 'banner', uploadId))
          .then(() => this.$emit('refresh'))
          .catch(error => this.$emit('error', error))
          .finally(() => {
            this.sendingImage = false;
          });
      }
    },
  },
};
</script>