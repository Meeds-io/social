<template>
  <v-app>
    <v-card
      class="application-body"
      flat>
      <v-hover>
        <v-img
          slot-scope="{ hover }"
          :lazy-src="bannerUrl || ''"
          :src="bannerUrl || ''"
          :min-height="minHeight"
          :max-height="height"
          :class="!bannerUrl && 'primary'"
          id="spaceAvatarImg"
          height="auto"
          min-width="100%"
          class="d-flex application-border-radius"
          eager>
          <div
            v-if="admin"
            v-show="hover"
            class="d-flex flex-grow-1 position-absolute full-height full-width">
            <div class="me-2 ms-auto my-auto mt-sm-2 mb-sm-0">
              <v-btn
                v-show="!isDefaultBanner && hover"
                :title="$t('UIPopupBannerUploader.title.deleteBanner')"
                id="spaceBannerDeleteButton"
                class="changeBannerButton mask-color border-color"
                outlined
                icon
                dark
                @click="removeBanner">
                <v-icon size="18" color="error">mdi-delete</v-icon>
              </v-btn>
              <v-btn
                v-show="hover"
                ref="bannerInput"
                id="spaceBannerEditButton"
                class="changeBannerButton mask-color border-color"
                icon
                outlined
                dark
                @click="$refs.imageCropDrawer.open()">
                <v-icon size="18">fas fa-file-image</v-icon>
              </v-btn>
            </div>
          </div>
        </v-img>
      </v-hover>
      <image-crop-drawer
        v-if="admin"
        ref="imageCropDrawer"
        :crop-options="cropOptions"
        :max-file-size="maxUploadSizeInBytes"
        :src="bannerUrl"
        max-image-width="1280"
        drawer-title="UIPopupBannerUploader.title.ChangeBanner"
        @input="uploadBanner" />
    </v-card>
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
    cropOptions: {
      aspectRatio: 1280 / 175,
      viewMode: 1,
    },
  }),
  computed: {
    isDefaultBanner() {
      return this.bannerUrl && this.bannerUrl.includes('/portal/rest/v1/social/spaceTemplates/');
    },
    maxUploadSizeInBytes() {
      return this.maxUploadSize * ONE_KB * ONE_KB;
    },
    minHeight() {
      return this.isMobile && 36 || 174;
    },
    height() {
      let height = 175;
      if (this.isMobile) {
        height -= 50;
      }
      return height;
    },
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
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
  },
};
</script>