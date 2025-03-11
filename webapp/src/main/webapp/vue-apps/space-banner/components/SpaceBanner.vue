<template>
  <v-app>
    <v-hover
      v-model="hover"
      :disabled="!admin">
      <v-responsive
        :aspect-ratio="cropOptions.aspectRatio"
        class="application-body application-dimension overflow-hidden"
        :class="$root.hasImages && 'transparent' || 'primary'">
        <space-banner-setting-buttons
          v-if="admin"
          :default-banner="defaultBanner"
          :hover="hover"
          @edit="$refs.imageCropDrawer.open()"
          @remove="removeBanner" />
        <div class="d-flex fill-height fill-width">
          <img
            v-if="bannerUrl"
            :alt="spaceDisplayName"
            class="fill-height fill-width border-box-sizing"
            height="100%"
            :src="bannerUrl"
            width="100%">
        </div>
      </v-responsive>
    </v-hover>
    <template v-if="admin">
      <image-crop-drawer
        ref="imageCropDrawer"
        :crop-options="cropOptions"
        drawer-title="UIPopupBannerUploader.title.ChangeBanner"
        :max-file-size="maxUploadSizeInBytes"
        max-image-width="1280"
        :src="bannerUrl"
        @input="uploadBanner" />
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
      defaultBanner () {
        return this.bannerUrl?.includes?.('/social/images/');
      },
      maxUploadSizeInBytes () {
        return this.maxUploadSize * ONE_KB * ONE_KB;
      },
    },
    watch: {
      errorMessage () {
        if (this.errorMessage) {
          this.$root.$emit('alert-message', this.errorMessage, 'error');
        }
      },
    },
    mounted () {
      this.$root.$applicationLoaded();
    },
    methods: {
      uploadBanner (uploadId) {
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
      handleError (error) {
        if (error) {
          if (String(error).indexOf(this.$uploadService.bannerExcceedsLimitError) >= 0) {
            this.errorMessage = this.$t('spaceHeader.label.bannerExcceededAllowedSize', { 0: this.maxUploadSize });
          } else {
            this.errorMessage = String(error);
          }
        }
      },
      removeBanner () {
        return this.$spaceService.updateSpace({
          id: eXo.env.portal.spaceId,
          bannerId: 'DEFAULT_BANNER',
        })
          .then(space => {
            this.$emit('banner-changed', space.bannerUrl);
            this.$root.$emit('alert-message', this.$t('UIPopupBannerUploader.title.BannerDeleted'), 'success');
          });
      },
      urlVerify (url) {
        if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/)) {
          url = `//${url}`;
        }
        return url ;
      },
    },
  };
</script>