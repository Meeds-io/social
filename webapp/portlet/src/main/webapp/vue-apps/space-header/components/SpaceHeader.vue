<template>
  <v-app :class="hasNavigations && 'hasNavigations' | ''">
    <v-card
      color="transparent"
      class="card-border-radius overflow-hidden"
      flat>
      <v-hover>
        <v-img
          slot-scope="{ hover }"
          :lazy-src="bannerUrl || ''"
          :src="bannerUrl || ''"
          :min-height="36"
          :max-height="height"
          id="spaceAvatarImg"
          height="auto"
          min-width="100%"
          class="d-flex"
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
                class="changeBannerButton border-color"
                outlined
                icon
                dark
                @click="removeBanner">
                <v-icon size="18">mdi-delete</v-icon>
              </v-btn>
              <v-btn
                v-show="hover"
                ref="bannerInput"
                id="spaceBannerEditButton"
                class="changeBannerButton border-color"
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
      <v-tabs
        v-if="hasNavigations"
        :value="selectedNavigationUri"
        active-class="SelectedTab"
        class="mx-auto"
        show-arrows
        center-active
        slider-size="4"
        @change="$root.$emit('application-cache')">
        <v-tab
          v-for="nav in navigations"
          :key="nav.id"
          :value="nav.id"
          :href="urlVerify(nav.uri)"
          :target="nav?.target === 'SAME_TAB' && '_self' || '_blank'"
          class="spaceNavigationTab">
          {{ nav.label }}
        </v-tab>
      </v-tabs>
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
    navigations: {
      type: Array,
      default: () => [],
    },
    selectedNavigationUri: {
      type: String,
      default: null,
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
    height() {
      let height = this.hasNavigations ? 143 : 175;
      if (this.isMobile) {
        height -= 50;
      }
      return height;
    },
    hasNavigations() {
      return this.navigations && this.navigations.length;
    },
    isMobile() {
      return this.$vuetify.breakpoint.xs;
    },
  },
  watch: {
    errorMessage() {
      if (this.errorMessage) {
        this.$root.$emit('alert-message', this.errorMessage, 'error');
      }
    },
  },
  created() {
    document.addEventListener('refreshSpaceNavigations', () => {
      this.$spaceService.getSpaceNavigations(eXo.env.portal.spaceId)
        .then(data => {
          // Compute URI of nodes of old navigation
          if (data && data.length) {
            data.forEach(nav => {
              const oldNav = this.navigations.find(oldNav => oldNav.id === nav.id);
              if (oldNav) {
                nav.uri = oldNav.uri;
              } else if (nav.uri && nav.uri.indexOf('/') >= 0) {
                nav.uri = nav.uri.split('/')[1];
              }
            });
            this.navigations = data;
          }
          return this.$nextTick();
        })
        .then(() => this.$root.$emit('application-cache'));
    });
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