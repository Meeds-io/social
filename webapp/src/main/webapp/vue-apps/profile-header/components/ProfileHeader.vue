<template>
  <v-app :class="owner && 'profileHeaderOwner' || 'profileHeaderOther'">
    <v-hover>
      <template #default="{ hover }">
        <div class="application-body">
          <v-card
            class="d-flex position-relative overflow-hidden"
            flat
            max-height="175"
            tile>
            <img
              alt=""
              class="profileBannerImg application-border-radius-top"
              height="auto"
              lazy
              :src="user && user.banner"
              width="100%">
            <profile-header-banner-button
              v-if="owner"
              class="justify-end full-width position-absolute t-0 r-3 pt-3"
              :hover="hover"
              :user="user"
              @edit="editBanner"
              @refresh="refresh" />
          </v-card>
          <v-card
            class="d-flex flex-column flex-md-row border-color px-4" 
            flat
            tile>
            <v-card
              class="flex-shrink-0 position-relative me-2"
              flat
              height="11vw"
              max-height="70"
              :max-width="165"
              tile
              :width="imageSize">
              <v-card
                class="position-absolute z-index-two b-0 mb-2 mb-md-2"
                color="transparent"
                flat
                tile>
                <profile-header-avatar
                  :hover="hover"
                  :owner="owner"
                  :size="imageSize"
                  :user="user"
                  @edit="editAvatar" />
              </v-card>
            </v-card>
            <v-card
              class="d-flex flex-column flex-sm-row flex-grow-1"
              flat
              min-height="70"
              tile>
              <profile-header-text
                class="d-flex flex-grow-0 text-truncate"
                :user="user" />
              <profile-header-actions
                v-if="!owner"
                class="profileHeader flex-grow-1 flex-shrink-0 d-flex flex-row justify-start justify-sm-end my-auto"
                :hover="hover"
                :user="user"
                @refresh="refresh" />
            </v-card>
          </v-card>
        </div>
      </template>
    </v-hover>
    <image-crop-drawer
      v-if="owner"
      ref="imageCropDrawer"
      :circle="imageCropperCircle"
      :crop-options="imageCropperOptions"
      :drawer-title="imageCropperDrawerTitle"
      :max-file-size="maxUploadSizeInBytes"
      :max-image-width="maxImageWidth"
      :src="imageCropperSrc"
      @input="uploadImage" />
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
    }),
    computed: {
      small () {
        return this.$vuetify.breakpoint.mdAndDown;
      },
      large () {
        return this.$vuetify.breakpoint.lgAndUp;
      },
      xlarge () {
        return this.$vuetify.breakpoint.xlAndUp;
      },
      imageSize () {
        return '15vw';
      },
      maxImageViewHeight () {
        return this.large && '175px' || 'calc(16.6vw - 40px)';
      },
      maxUploadSizeInBytes () {
        return this.maxUploadSize * 1024 * 1024;
      },
      imageCropperDrawerTitle () {
        return this.imageType === 'avatar' && this.$t('UIChangeAvatarContainer.label.ChangeAvatar') || this.$t('UIPopupBannerUploader.title.ChangeBanner');
      },
      imageCropperSrc () {
        return this.user && (this.imageType === 'avatar' && `${this.user.avatar}&size=0` || `${this.user.banner}&size=0`);
      },
      imageCropperCircle () {
        return this.imageType === 'avatar';
      },
      imageCropperOptions () {
        return this.imageType === 'avatar' && {
          aspectRatio: 1,
          viewMode: 1,
        } || {
          aspectRatio: 1280 / 175,
          viewMode: 1,
        };
      },
      maxImageWidth () {
        return this.imageType === 'avatar' && 350 || 1280;
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
      this.refresh();
      document.addEventListener('userModified', event => {
        if (event && event.detail && event.detail !== this.user) {
          this.user = Object.assign({}, this.user, event.detail);
          this.$nextTick().then(() => this.$root.$emit('application-loaded'));
        }
      });
    },
    methods: {
      editAvatar () {
        this.imageType = 'avatar';
        this.$nextTick()
          .then(() => this.$refs.imageCropDrawer.open());
      },
      editBanner () {
        this.imageType = 'banner';
        this.$nextTick()
          .then(() => this.$refs.imageCropDrawer.open());
      },
      uploadImage (uploadId) {
        if (this.imageType === 'avatar') {
          this.uploadAvatar(uploadId);
        } else if (this.imageType === 'banner') {
          this.uploadBanner(uploadId);
        }
      },
      uploadAvatar (uploadId) {
        if (uploadId) {
          return this.$userService.updateProfileField(eXo.env.portal.userName, 'avatar', uploadId)
            .then(() => this.avatarUpdated())
            .catch(this.handleError)
            .finally(() => this.imageType = null);
        }
      },
      uploadBanner (uploadId) {
        if (uploadId) {
          return this.$userService.updateProfileField(eXo.env.portal.userName, 'banner', uploadId)
            .then(() => this.bannerUpdated())
            .catch(this.handleError)
            .finally(() => this.imageType = null);
        }
      },
      avatarUpdated () {
        return this.refresh()
          .then(() => {
            document.dispatchEvent(new CustomEvent('userModified', { detail: this.user }));
            this.$root.$emit('alert-message', this.$t('UIChangeAvatarContainer.label.AvatarUpdated') ,'success');
          });
      },
      bannerUpdated () {
        return this.refresh()
          .then(() => {
            document.dispatchEvent(new CustomEvent('userModified', { detail: this.user }));
            this.$root.$emit('alert-message', this.$t('UIPopupBannerUploader.title.BannerUpdated') ,'success');
          });
      },
      refresh () {
        return this.$userService.getUser(eXo.env.portal.profileOwner, 'relationshipStatus')
          .then(user => {
            this.user = user;
            return this.$nextTick();
          })
          .catch(e => {
            console.warn('Error while retrieving user details', e);  
          })
          .finally(() => this.$nextTick().then(() => this.$root.$applicationLoaded()));
      },
      handleError (error) {
        if (error) {
          if (String(error).indexOf(this.$uploadService.avatarExcceedsLimitError) >= 0) {
            this.errorMessage = this.$t('profileHeader.label.avatarExcceededAllowedSize', { 0: this.maxUploadSize });
          } else if (String(error).indexOf(this.$uploadService.bannerExcceedsLimitError) >= 0) {
            this.errorMessage = this.$t('profileHeader.label.bannerExcceededAllowedSize', { 0: this.maxUploadSize });
          } else {
            this.errorMessage = String(error);
          }
        }
      },
    },
  };
</script>