<template>
  <v-file-input
    v-if="!sendingImage"
    v-show="hover"
    ref="bannerInput"
    prepend-icon="fa-file-image"
    class="changeBannerButton me-4"
    accept="image/*"
    clearable
    @change="uploadBanner" />
</template>

<script>
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
          .then(uploadId => this.$spaceService.updateSpace({
            id: eXo.env.portal.spaceId,
            bannerId: uploadId,
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