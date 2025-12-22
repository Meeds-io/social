<template>
  <v-app
    :class="{ 'profileHeaderOwner': owner, 'profileHeaderOther': !owner }">
    <v-hover>
      <div slot-scope="{ hover }" class="application-body">
        <v-card
          :max-height="bannerMaxHeight"
          :height="bannerHeight"
          min-height="60"
          class="d-flex position-relative overflow-hidden"
          tile
          flat>
          <v-img
            :src="user?.banner"
            :max-height="bannerMaxHeight"
            :height="bannerHeight"
            min-height="60"
            width="100%"
            class="profileBannerImg application-border-radius-top"
            lazy />
          <profile-header-banner-button
            :owner="owner"
            :user="user"
            :is-admin="isAdmin"
            :hover="hover"
            class="justify-end full-width position-absolute t-0 r-3 pt-3"
            @edit="editBanner"
            @refresh="refresh"
            @open-settings="openHeaderSettings" />
        </v-card>
        <v-card
          class="d-flex flex-column px-4"
          :class="{ 'flex-md-row': !containerMD && containerBasedBreakpoints || !containerBasedBreakpoints }"
          flat
          tile>
          <v-card
            :width="avatarSize"
            :max-width="avatarMaxSize"
            :max-height="avatarMaxSize/2"
            height="11vw"
            class="flex-shrink-0 position-relative me-2"
            flat
            tile>
            <v-card
              class="position-absolute z-index-two b-0 mb-2 mb-md-2"
              color="transparent"
              flat
              tile>
              <profile-header-avatar
                :user="user"
                :owner="owner"
                :hover="hover"
                :size="avatarSize"
                :max-size="avatarMaxSize"
                :min-size="avatarMinSize"
                @edit="editAvatar" />
            </v-card>
          </v-card>
          <v-card
            class="d-flex flex-column mb-2 flex-grow-1"
            :class="{'flex-sm-row': !containerSM && containerBasedBreakpoints || !containerBasedBreakpoints}"
            flat
            tile>
            <profile-header-text
              :user="user"
              :display-option="displayOption"
              class="d-flex flex-grow-0 text-truncate" />
            <profile-header-actions
              v-if="useActions && !owner"
              :user="user"
              :hover="hover"
              class="profileHeader flex-grow-1 flex-shrink-0 d-flex flex-row justify-start justify-sm-end my-auto"
              @refresh="refresh" />
          </v-card>
        </v-card>
      </div>
    </v-hover>
    <image-crop-drawer
      v-if="owner"
      ref="imageCropDrawer"
      :src="imageCropperSrc"
      :circle="imageCropperCircle"
      :crop-options="imageCropperOptions"
      :drawer-title="imageCropperDrawerTitle"
      :max-file-size="maxUploadSizeInBytes"
      :max-image-width="maxImageWidth"
      @apply="updateImage" />
    <profile-header-settings-drawer
      v-if="isAdmin"
      :save-settings-url="$root.settings.saveSettingsUrl"
      :saved-settings="{
        avatarMaxSize: this.avatarMaxSize,
        avatarMinSize: this.avatarMinSize,
        bannerMaxHeight: this.bannerMaxHeight,
        displayOption: this.displayOption,
        bannerHeight: this.bannerHeight
      }"
      @updated="headerSettingsUpdated"
      ref="headerSettingsDrawer" />
  </v-app>    
</template>
<script>
export default {
  props: {
    maxUploadSize: {
      type: Number,
      default: () => 2,
    },
  },
  data: () => ({
    user: null,
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    errorMessage: null,
    imageType: null,
    avatarSize: '15vw',
    appWidth: null
  }),
  computed: {
    containerSM() {
      return this.appWidth < this.$vuetify.breakpoint.thresholds.md;
    },
    containerMD() {
      return this.appWidth < this.$vuetify.breakpoint.thresholds.sm;
    },
    avatarMaxSize() {
      return this.$root?.settings?.avatarMaxSize;
    },
    avatarMinSize() {
      return this.$root?.settings?.avatarMinSize;
    },
    bannerMaxHeight() {
      return this.$root?.settings?.bannerMaxHeight;
    },
    bannerHeight() {
      return this.$root?.settings?.bannerHeight;
    },
    useActions() {
      return this.$root?.settings?.useActions;
    },
    containerBasedBreakpoints() {
      return this.$root?.settings?.containerBasedBreakpoints;
    },
    displayOption() {
      return this.$root?.settings?.displayOption;
    },
    maxUploadSizeInBytes() {
      return this.maxUploadSize * 1024 * 1024;
    },
    imageCropperDrawerTitle() {
      return this.imageType === 'avatar' && this.$t('UIChangeAvatarContainer.label.ChangeAvatar') || this.$t('UIPopupBannerUploader.title.ChangeBanner');
    },
    imageCropperSrc() {
      return this.user && (this.imageType === 'avatar' && `${this.user.avatar}&size=0` || `${this.user.banner}&size=0`);
    },
    imageCropperCircle() {
      return this.imageType === 'avatar';
    },
    imageCropperOptions() {
      return this.imageType === 'avatar' && {
        aspectRatio: 1,
        viewMode: 1,
      } || {
        aspectRatio: 1280 / 175,
        viewMode: 1,
      };
    },
    maxImageWidth() {
      return this.imageType === 'avatar' && 350 || 1280;
    },
    isAdmin() {
      return this.user?.isAdmin;
    }
  },
  watch: {
    errorMessage() {
      if (this.errorMessage) {
        this.$root.$emit('alert-message', this.errorMessage, 'error');
      }
    }
  },
  created() {
    window.addEventListener('resize', this.calculateAppWidth);
  },
  mounted() {
    this.calculateAppWidth();
    this.refresh();
    document.addEventListener('userModified', event => {
      if (event && event.detail && event.detail !== this.user) {
        this.user = Object.assign({}, this.user, event.detail);
        this.$nextTick().then(() => this.$root.$emit('application-loaded'));
      }
    });
  },
  methods: {
    editAvatar() {
      this.imageType = 'avatar';
      this.$nextTick()
        .then(() => this.$refs.imageCropDrawer.open());
    },
    editBanner() {
      this.imageType = 'banner';
      this.$nextTick()
        .then(() => this.$refs.imageCropDrawer.open());
    },
    uploadImage(uploadId) {
      if (this.imageType === 'avatar') {
        this.uploadAvatar(uploadId);
      } else if (this.imageType === 'banner') {
        this.uploadBanner(uploadId);
      }
    },
    uploadAvatar(uploadId) {
      if (uploadId) {
        return this.$userService.updateProfileField(eXo.env.portal.userName, 'avatar', uploadId)
          .then(() => this.avatarUpdated())
          .catch(this.handleError)
          .finally(() => this.imageType = null);
      }
    },
    uploadBanner(uploadId) {
      if (uploadId) {
        return this.$userService.updateProfileField(eXo.env.portal.userName, 'banner', uploadId)
          .then(() => this.bannerUpdated())
          .catch(this.handleError)
          .finally(() => this.imageType = null);
      }
    },
    avatarUpdated() {
      return this.refresh()
        .then(() => {
          document.dispatchEvent(new CustomEvent('userModified', {detail: this.user}));
          this.$root.$emit('alert-message', this.$t('UIChangeAvatarContainer.label.AvatarUpdated') ,'success');
        });
    },
    bannerUpdated() {
      return this.refresh()
        .then(() => {
          document.dispatchEvent(new CustomEvent('userModified', {detail: this.user}));
          this.$root.$emit('alert-message', this.$t('UIPopupBannerUploader.title.BannerUpdated') ,'success');
        });
    },
    refresh() {
      return this.$userService.getUser(eXo.env.portal.profileOwner, 'relationshipStatus,settings')
        .then(user => {
          this.user = user;
          return this.$nextTick();
        })
        .catch((e) => {
          console.warn('Error while retrieving user details', e); // eslint-disable-line no-console
        })
        .finally(() => this.$nextTick().then(() => this.$root.$applicationLoaded()));
    },
    handleError(error) {
      if (error) {
        if (String(error).indexOf(this.$uploadService.avatarExcceedsLimitError) >= 0) {
          this.errorMessage = this.$t('profileHeader.label.avatarExcceededAllowedSize', {0: this.maxUploadSize});
        } else if (String(error).indexOf(this.$uploadService.bannerExcceedsLimitError) >= 0) {
          this.errorMessage = this.$t('profileHeader.label.bannerExcceededAllowedSize', {0: this.maxUploadSize});
        } else {
          this.errorMessage = String(error);
        }
      }
    },
    openHeaderSettings() {
      this.$refs.headerSettingsDrawer.open();
    },
    headerSettingsUpdated(settings) {
      this.$root.settings.displayOption = settings.displayOption;
      this.$root.settings.avatarMinSize = settings.avatarMinSize;
      this.$root.settings.avatarMaxSize = settings.avatarMaxSize;
      this.$root.settings.bannerMaxHeight = settings.bannerMaxHeight;
      this.$root.settings.bannerHeight = settings.bannerHeight;
      this.$refs.headerSettingsDrawer.close();
    },
    calculateAppWidth() {
      this.appWidth = document.getElementById('ProfileHeader')?.clientWidth;
    },
    updateImage(image) {
      this.uploadImage(image.uploadId);
    }
  },
};
</script>
