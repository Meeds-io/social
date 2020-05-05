<template>
  <v-file-input
    v-if="!sendingImage"
    v-show="hover"
    ref="bannerInput"
    prepend-icon="fa-file-image"
    class="changeBannerButton mr-4"
    accept="image/*"
    clearable
    @change="uploadBanner" />
</template>

<script>
import {updateSpace} from '../js/SpaceService.js'; 
import * as uploadService from '../js/UploadService.js'; 

const DEFAULT_MAX_UPLOAD_SIZE_IN_MB = 2;

export default {
  props: {
    maxUploadSize: {
      type: Number,
      default: () => DEFAULT_MAX_UPLOAD_SIZE_IN_MB,
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
          .then(uploadId => updateSpace(eXo.env.portal.spaceId, {
            'bannerId': uploadId,
          }))
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