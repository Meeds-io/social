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
import * as userService from '../../common/js/UserService.js'; 
import * as uploadService from '../../common/js/UploadService.js'; 

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
        if (file.size > this.maxUploadSize) {
          this.$emit('error', uploadService.bannerExcceedsLimitError);
          return;
        }
        this.sendingImage = true;
        return uploadService.upload(file)
          .then(uploadId => userService.updateProfileField('banner', uploadId))
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