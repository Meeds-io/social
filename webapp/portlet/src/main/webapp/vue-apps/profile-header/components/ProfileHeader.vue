<template>
  <v-app :class="owner && 'profileHeaderOwner' || 'profileHeaderOther'">
    <v-hover>
      <div slot-scope="{ hover }">
        <div class="d-flex flex-column full-height position-absolute z-index-two">
          <profile-header-avatar
            :user="user"
            :owner="owner"
            :hover="hover"
            :size="imageSize"
            class="mt-4 mt-sm-auto ms-4"
            @edit="editAvatar" />
          <v-card
            color="transparent"
            min-height="35px"
            flat />
        </div>
        <v-card
          max-height="calc(16.6vw - 40px)"
          min-height="88"
          class="d-flex"
          tile
          flat>
          <v-img
            :lazy-src="user && user.banner"
            :src="user && user.banner"
            transition="none"
            class="profileBannerImg d-flex white"
            height="auto"
            min-width="100%"
            eager>
            <profile-header-banner-button
              v-if="owner"
              :user="user"
              :hover="hover"
              class="justify-end me-2 mt-2 me-sm-4 mt-sm-4"
              @edit="editBanner"
              @refresh="refresh" />
          </v-img>
        </v-card>
        <v-card
          class="d-flex flex-row"
          color="white"
          min-height="70"
          flat
          tile>
          <v-card
            :width="imageSize"
            max-width="350"
            class="d-flex flex-row ms-4 flex-shrink-0"
            color="white"
            flat
            tile />
          <div class="d-flex flex-grow-0 text-truncate">
            <profile-header-text
              :user="user"
              class="mt-auto" />
          </div>
          <div class="flex-grow-1 flex-shrink-0 d-flex flex-row justify-end pe-2 profileHeader">
            <profile-header-actions
              v-if="!owner"
              :user="user"
              :hover="hover"
              @refresh="refresh" />
          </div>
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
      @input="uploadImage" />
    <alert-notifications v-if="owner" />
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
    imageSize: '15vw',
  }),
  computed: {
    mobile() {
      return this.$vuetify.breakpoint.name === 'xs';
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
  },
  watch: {
    errorMessage() {
      if (this.errorMessage) {
        this.$root.$emit('alert-message', this.errorMessage, 'error');
      }
    },
  },
  mounted() {
    this.refresh();
    document.addEventListener('userModified', event => {
      if (event && event.detail && event.detail !== this.user) {
        Object.assign(this.user, event.detail);
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
      return this.$userService.getUser(eXo.env.portal.profileOwner, 'relationshipStatus')
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
  },
};
</script>