<template>
  <v-avatar
    :class="skeleton && 'skeleton-background' || owner && hover && 'profileHeaderAvatarHoverEdit'"
    class="align-start flex-grow-0 ml-3 my-3 profileHeaderAvatar"
    size="165">
    <v-img :src="avatarData || user && user.avatar || ''" />
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
    save: {
      type: Boolean,
      default: () => false,
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
        if (file.size > this.maxUploadSize) {
          this.$emit('error', uploadService.avatarExcceedsLimitError);
          return;
        }
        this.sendingImage = true;
        const thiss = this;
        return uploadService.upload(file)
          .then(uploadId => {
            if (this.save) {
              return userService.updateProfileField(eXo.env.portal.userName, 'avatar', uploadId);
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