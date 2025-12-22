<template>
  <v-app>
    <v-hover v-model="hover" :disabled="!admin">
      <v-responsive
        :aspect-ratio="cropOptions.aspectRatio"
        :class="$root.hasImages && 'transparent' || 'primary'"
        class="application-body application-dimension overflow-hidden">
        <space-banner-setting-buttons
          v-if="admin"
          :hover="hover"
          :default-banner="defaultBanner"
          class="z-index-one"
          @edit="$refs.imageCropDrawer.open()"
          @remove="removeBanner" />
        <div class="d-flex fill-height fill-width">
          <img
            v-if="bannerUrl"
            :src="bannerUrl"
            :alt="spaceDisplayName"
            style="min-width: 100%;min-height: 100%;"
            width="100%"
            height="100%"
            class="border-box-sizing absolute-all-center">
        </div>
      </v-responsive>
    </v-hover>
    <template v-if="admin">
      <image-crop-drawer
        ref="imageCropDrawer"
        :crop-options="cropOptions"
        :max-file-size="maxUploadSizeInBytes"
        :src="bannerUrl"
        max-image-width="1280"
        drawer-title="UIPopupBannerUploader.title.ChangeBanner"
        @apply="updateImage" />
    </template>
  </v-app>
</template>
<script>
const DEFAULT_MAX_UPLOAD_SIZE_IN_MB = 2;
const ONE_KB = 1024;

export default {
  props: {
    admin: {
      type: Boolean,
      default: false,
    },
    maxUploadSize: {
      type: Number,
      default: () => DEFAULT_MAX_UPLOAD_SIZE_IN_MB,
    },
    bannerUrl: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    errorMessage: null,
    hover: false,
    spaceDisplayName: eXo.env.portal.spaceDisplayName,
    cropOptions: {
      aspectRatio: 1280 / 175,
      viewMode: 1,
    },
  }),
  computed: {
    defaultBanner() {
      return this.bannerUrl?.includes?.('/social/images/');
    },
    maxUploadSizeInBytes() {
      return this.maxUploadSize * ONE_KB * ONE_KB;
    },
  },
  watch: {
    errorMessage() {
      if (this.errorMessage) {
        this.$root.$emit('alert-message', this.errorMessage, 'error');
      }
    },
  },
  mounted() {
    this.$root.$applicationLoaded();
  },
  methods: {
    uploadBanner(uploadId) {
      return this.$spaceService.updateSpace({
        id: eXo.env.portal.spaceId,
        bannerId: uploadId,
      })
        .then(space => {
          this.$emit('banner-changed', space.bannerUrl);
          this.$root.$emit('alert-message', this.$t('UIPopupBannerUploader.title.BannerUpdated'), 'success');
        })
        .catch(this.handleError);
    },
    handleError(error) {
      if (error) {
        if (String(error).indexOf(this.$uploadService.bannerExcceedsLimitError) >= 0) {
          this.errorMessage = this.$t('spaceHeader.label.bannerExcceededAllowedSize', {0: this.maxUploadSize});
        } else {
          this.errorMessage = String(error);
        }
      }
    },
    removeBanner() {
      return this.$spaceService.updateSpace({
        id: eXo.env.portal.spaceId,
        bannerId: 'DEFAULT_BANNER',
      })
        .then(space => {
          this.$emit('banner-changed', space.bannerUrl);
          this.$root.$emit('alert-message', this.$t('UIPopupBannerUploader.title.BannerDeleted'), 'success');
        });
    },
    urlVerify(url) {
      if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/)) {
        url = `//${url}`;
      }
      return url ;
    },
    updateImage(image) {
      this.uploadBanner(image.uploadId);
    }
  },
};
</script>