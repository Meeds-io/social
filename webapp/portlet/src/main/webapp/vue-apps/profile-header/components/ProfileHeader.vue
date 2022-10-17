<template>
  <v-app 
    :class="owner && 'profileHeaderOwner' || 'profileHeaderOther'">
    <v-hover>
      <v-img
        slot-scope="{ hover }"
        :lazy-src="user && user.banner"
        :src="user && user.banner"
        transition="none"
        class="profileBannerImg d-flex"
        min-height="240px"
        height="240px"
        max-height="240px"
        eager>
        <v-flex fill-height>
          <v-layout>
            <v-flex class="d-flex profileHeaderTitle">
              <exo-confirm-dialog
                ref="errorUploadDialog"
                :message="errorMessage"
                :title="$t('profileHeader.title.errorUploadingImage')"
                :ok-label="$t('profileHeader.label.ok')" />
              <v-hover>
                <profile-header-avatar
                  slot-scope="{ profileHover }"
                  :user="user"
                  :max-upload-size="maxUploadSizeInBytes"
                  :owner="owner"
                  :hover="hover || profileHover"
                  save
                  @refresh="avatarUpdated"
                  @error="handleError" />
              </v-hover>
              <div class="profileHeader">
                <div class="profileHeaderText align-start d-flex flex-grow-0">
                  <profile-header-text
                    :user="user"
                    :skeleton="skeleton"
                    :class="skeleton && 'skeleton-text' || ''"
                    class="ma-auto pb-0" />
                </div>
                <div class="flex-grow-1"></div>
                <div v-if="!skeleton" class="d-flex flex-row justify-end pe-6">
                  <profile-header-banner-button
                    v-if="owner"
                    :user="user"
                    :max-upload-size="maxUploadSizeInBytes"
                    :owner="owner"
                    :hover="hover"
                    @refresh="refresh"
                    @error="handleError" />
                  <profile-header-actions
                    v-else
                    :user="user"
                    :owner="owner"
                    :hover="hover"
                    @refresh="refresh" />
                </div>
              </div>
            </v-flex>
          </v-layout>
        </v-flex>
      </v-img>
    </v-hover>
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
  }),
  computed: {
    mobile() {
      return this.$vuetify.breakpoint.name === 'xs';
    },
    maxUploadSizeInBytes() {
      return this.maxUploadSize * 1024 * 1024;
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
    avatarUpdated() {
      this.refresh().then(() => {
        document.dispatchEvent(new CustomEvent('userModified', {detail: this.user}));
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
        this.$refs.errorUploadDialog.open();
      }
    },
  },
};
</script>