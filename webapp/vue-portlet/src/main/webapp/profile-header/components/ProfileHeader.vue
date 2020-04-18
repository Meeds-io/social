<template>
  <v-app :class="owner && 'profileHeaderOwner' || 'profileHeaderOther'">
    <v-hover :disabled="skeleton">
      <v-img
        slot-scope="{ hover }"
        :src="!skeleton && user && user.banner || ''"
        :class="skeleton && 'skeleton-background' || ''"
        class="profileBannerImg d-flex"
        min-height="240px"
        height="240px"
        max-height="240px"
        eager>
        <v-flex fill-height>
          <v-layout>
            <v-flex class="d-flex profileHeaderTitle">
              <exo-confirm-dialog
                ref="errorDialog"
                :message="errorMessage"
                :title="$t('profileHeader.title.errorUploadingImage')"
                :ok-label="$t('profileHeader.label.ok')" />
              <v-hover :disabled="skeleton">
                <profile-header-avatar
                  slot-scope="{ hover }"
                  :user="user"
                  :max-upload-size="maxUploadSizeInBytes"
                  :skeleton="skeleton"
                  :owner="owner"
                  :hover="hover"
                  @refresh="refresh"
                  @error="handleError" />
              </v-hover>
              <div class="profileHeaderText align-start d-flex flex-grow-0">
                <v-flex class="ma-auto pb-10">
                  <v-card-title
                    :class="skeleton && 'skeleton-background skeleton-text skeleton-text-width skeleton-text-height pa-0 my-3' || ''"
                    class="headline white--text">
                    {{ !skeleton && user && user.fullname || '&nbsp;' }}
                  </v-card-title>
                  <v-card-subtitle
                    :class="skeleton && 'skeleton-background skeleton-text skeleton-text-width skeleton-text-height pa-0 my-3' || ''"
                    class="subtitle white--text"
                    dark>
                    {{ !skeleton && user && user.position || '&nbsp;' }}
                  </v-card-subtitle>
                </v-flex>
              </div>
              <div class="flex-grow-1"></div>
              <div class="d-flex flex-grow-0 justify-end pr-4">
                <profile-header-banner-button
                  v-if="owner"
                  :user="user"
                  :max-upload-size="maxUploadSizeInBytes"
                  :skeleton="skeleton"
                  :owner="owner"
                  :hover="hover"
                  @refresh="refresh"
                  @error="handleError" />
                <profile-header-actions
                  v-if="!owner"
                  :user="user"
                  :skeleton="skeleton"
                  :owner="owner"
                  :hover="hover"
                  @refresh="refresh" />
              </div>
            </v-flex>
          </v-layout>
        </v-flex>
      </v-img>
    </v-hover>
  </v-app>    
</template>

<script>
import * as userService from '../../common/js/UserService.js'; 
import * as uploadService from '../../common/js/UploadService.js'; 

export default {
  props: {
    maxUploadSize: {
      type: Number,
      default: () => 2,
    },
  },
  data: () => ({
    user: null,
    skeleton: true,
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
  },
  methods: {
    refresh() {
      return userService.getUser(eXo.env.portal.profileOwner, 'relationshipStatus')
        .then(user => {
          this.user = user;
          return this.$nextTick();
        })
        .then(() => {
          this.skeleton = false;
        })
        .catch((e) => {
          console.warn('Error while retrieving user details', e); // eslint-disable-line no-console
        })
        .finally(() => {
          document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        });
    },
    handleError(error) {
      if (error) {
        if (String(error).indexOf(uploadService.avatarExcceedsLimitError) >= 0) {
          this.errorMessage = this.$t('profileHeader.label.avatarExcceededAllowedSize', {0: this.maxUploadSize});
        } else if (String(error).indexOf(uploadService.bannerExcceedsLimitError) >= 0) {
          this.errorMessage = this.$t('profileHeader.label.bannerExcceededAllowedSize', {0: this.maxUploadSize});
        } else {
          this.errorMessage = String(error);
        }
        this.$refs.errorDialog.open();
      }
    },
  },
};
</script>