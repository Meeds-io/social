<template>
  <v-avatar
    :class="skeleton && 'skeleton-background' || owner && hover && 'profileHeaderAvatarHoverEdit'"
    class="align-start flex-grow-0 ml-3 my-3 profileHeaderAvatar"
    size="165">
    <v-img :src="user && user.avatar || ''" />
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
import * as userService from '../../common/js/UserService.js'; 
import * as uploadService from '../../common/js/UploadService.js';

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
    skeleton: {
      type: Boolean,
      default: () => true,
    },
    owner: {
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
    uploadAvatar(file) {
      if (file && file.size) {
        if (file.size > this.maxUploadSize) {
          this.$emit('error', uploadService.avatarExcceedsLimitError);
          return;
        }
        this.sendingImage = true;
        return uploadService.upload(file)
          .then(uploadId => userService.updateProfileField('avatar', uploadId))
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