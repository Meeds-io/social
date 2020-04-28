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
  <v-avatar
    :class="skeleton && 'skeleton-background' || owner && hover && 'profileHeaderAvatarHoverEdit'"
    :size="size"
    class="align-start flex-grow-0 ml-3 my-3 profileHeaderAvatar">
    <v-img :src="!skeleton && (avatarData || user && user.avatar) || ''" />
    <v-file-input
      v-if="owner && !sendingImage"
      v-show="hover"
      ref="avatarInput"
      prepend-icon="mdi-camera"
      class="changeAvatarButton"
      accept="image/*"
      clearable
      @change="uploadAvatar" />
  </v-avatar>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: () => null,
    },
    maxUploadSize: {
      type: Number,
      default: () => 0,
    },
    save: {
      type: Boolean,
      default: () => false,
    },
    skeleton: {
      type: Boolean,
      default: () => false,
    },
    owner: {
      type: Boolean,
      default: () => true,
    },
    hover: {
      type: Boolean,
      default: () => false,
    },
    size: {
      type: Number,
      default: () => 165,
    },
    value: {
      type: String,
      default: () => null,
    },
  },
  data: () => ({
    sendingImage: false,
    avatarData: null,
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
    reset() {
      this.avatarData = null;
      this.sendingImage = false;
    },
    uploadAvatar(file) {
      if (file && file.size) {
        if (file.type && file.type.indexOf('image/') !== 0) {
          this.$emit('error', this.$t('profile.warning.message.fileType'));
          return;
        }
        if (file.size > this.maxUploadSize) {
          this.$emit('error', this.$uploadService.avatarExcceedsLimitError);
          return;
        }
        this.sendingImage = true;
        const thiss = this;
        return this.$uploadService.upload(file)
          .then(uploadId => {
            if (this.save) {
              return this.$userService.updateProfileField(eXo.env.portal.userName, 'avatar', uploadId);
            } else {
              const reader = new FileReader();
              reader.onload = (e) => {
                thiss.avatarData = e.target.result;
                thiss.$forceUpdate();
              };
              reader.readAsDataURL(file);
              this.$emit('input', uploadId);
            }
          })
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