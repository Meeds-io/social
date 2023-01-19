<template>
  <v-app :class="hasNavigations && 'hasNavigations' | ''">
    <v-card
      color="transparent"
      class="mb-6"
      flat>
      <v-hover>
        <v-img
          slot-scope="{ hover }"
          :lazy-src="bannerUrl || ''"
          :src="bannerUrl || ''"
          :max-height="height"
          height="auto"
          min-width="100%"
          class="d-flex"
          eager>
          <v-flex fill-height column>
            <v-layout>
              <v-flex class="d-flex spaceHeaderTitle">
                <div class="flex-grow-1"></div>
                <div class="d-flex flex-grow-0 justify-end pe-4">
                  <v-btn
                    v-if="admin"
                    v-show="hover"
                    ref="bannerInput"
                    class="changeBannerButton border-color me-4"
                    icon
                    outlined
                    dark
                    @click="$refs.imageCropDrawer.open()">
                    <v-icon size="18">fas fa-file-image</v-icon>
                  </v-btn>
                </div>
              </v-flex>
            </v-layout>
          </v-flex>
        </v-img>
      </v-hover>
      <image-crop-drawer
        v-if="admin"
        ref="imageCropDrawer"
        :crop-options="cropOptions"
        :max-file-size="maxUploadSizeInBytes"
        :src="bannerData || bannerUrl"
        drawer-title="UIPopupBannerUploader.title.ChangeBanner"
        @data="bannerData = $event"
        @input="uploadBanner" />
      <alert-notifications v-if="admin" />
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
          :href="nav.uri"
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
    bannerData: null,
    cropOptions: {
      aspectRatio: 1280 / 175,
      viewMode: 1,
    },
  }),
  computed: {
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
    refresh() {
      // Force refresh by using random query param 'updated'
      this.bannerUrl = `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${eXo.env.portal.spaceName}/banner?updated=${Math.random()}`;
      this.$nextTick().then(() => this.$root.$emit('application-cache'));
    },
    uploadBanner(uploadId) {
      return this.$spaceService.updateSpace({
        id: eXo.env.portal.spaceId,
        bannerId: uploadId,
      })
        .then(() => {
          this.refresh();
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
  },
};
</script>