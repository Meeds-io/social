<template>
  <div 
    class="d-flex flex-row changeBannerButton"
    v-if="hover">
    <div 
      v-if="!isDefaultBanner"
      class="changeBannerButtonIcon me-2">
      <v-btn 
        class="d-flex justify-center"
        dark
        outlined
        icon
        x-small
        rounded
        border
        @click="removeBanner">
        <v-icon
          class="text-center"
          icon
          rounded>
          mdi-delete
        </v-icon>
      </v-btn>
    </div>
    <div class="changeBannerButtonIcon">
      <v-file-input
        v-if="!sendingImage"
        ref="bannerInput"
        class="d-flex justify-center"
        prepend-icon="fa-file-image"
        rounded
        accept="image/*"
        clearable
        @change="uploadBanner" />
    </div>
  </div>
</template>

<script>
export default {
  props: {
    maxUploadSize: {
      type: Number,
      default: () => 2,
    },
    hover: {
      type: Boolean,
      default: () => false,
    },
    user: {
      type: Object,
      default: () => null,
    },
  },
  data: () => ({
    sendingImage: false,
    isDefaultBanner: false,
  }),
  watch: {
    sendingImage() {
      if (this.sendingImage) {
        document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      } else {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      }
    },
    user(){
      this.isDefaultBanner=this.user.banner.startsWith('/portal/rest/v1/social/users/default-image/');
    }
  },
  methods: {
    removeBanner(){     
      return this.$userService.updateProfileField(eXo.env.portal.userName, 'banner', null)
        .then(() => {
          this.isDefaultBanner = true;
          this.$emit('refresh');
        })
        .catch(error => this.$emit('error', error));
    },
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
          .then(() => {
            this.isDefaultBanner = false;
            this.$emit('refresh');
          })
          .catch(error => this.$emit('error', error))
          .finally(() => {
            this.sendingImage = false;
          });
      }
    },
  },
};
</script>