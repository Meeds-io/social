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
            <v-flex class="d-flex my-7">
              <exo-confirm-dialog
                ref="errorDialog"
                :message="errorMessage"
                :title="$t('profileHeader.title.errorUploadingImage')"
                :ok-label="$t('profileHeader.label.ok')" />
              <v-hover :disabled="skeleton">
                <v-avatar
                  slot-scope="{ hover }"
                  :class="skeleton && 'skeleton-background' || owner && hover && 'profileHeaderAvatarHoverEdit'"
                  class="align-start flex-grow-0 ml-3 my-3 profileHeaderAvatar"
                  size="165">
                  <v-img :src="!skeleton && user && user.avatar || ''" />
                  <v-file-input
                    v-if="owner && !sendingImage"
                    v-show="hover"
                    ref="avatarInput"
                    prepend-icon="mdi-camera"
                    class="changeAvatarButton"
                    accept="image/*"
                    clearable
                    @change="uploadAvatar" />
                </v-avatar>
              </v-hover>
              <div class="align-start d-flex flex-grow-0">
                <v-flex class="ma-auto pb-10">
                  <v-card-title
                    :class="skeleton && 'skeleton-background skeleton-text skeleton-text-width skeleton-text-height pa-0 my-3' || ''"
                    class="headline white--text">
                    {{ !skeleton && user && user.fullname || '&nbsp;' }}
                  </v-card-title>
                  <v-card-subtitle
                    :class="skeleton && 'skeleton-background skeleton-text skeleton-text-width skeleton-text-height pa-0 my-3' || ''"
                    class="white--text"
                    dark>
                    {{ !skeleton && user && user.position || '&nbsp;' }}
                  </v-card-subtitle>
                </v-flex>
              </div>
              <div class="flex-grow-1"></div>
              <div class="d-flex flex-grow-0 justify-end pr-4">
                <v-file-input
                  v-if="owner && !sendingImage"
                  v-show="hover"
                  ref="bannerInput"
                  prepend-icon="fa-file-image"
                  class="changeBannerButton mr-4"
                  accept="image/*"
                  clearable
                  @change="uploadBanner" />
                <div
                  id="profileHeaderActions"
                  :class="owner && 'profileHeaderOwnerActions' || 'profileHeaderOtherActions'"
                  class="mt-auto mr-3">
                  <template v-if="!owner">
                    <v-btn
                      v-for="(extension, i) in enabledProfileActionExtensions"
                      :key="i"
                      class="btn mx-2"
                      @click="extension.click(user)">
                      <i :class="extension.icon ? extension.icon : 'hidden'" class="uiIcon" />
                      <span class="d-none d-sm-flex">
                        {{ extension.title }}
                      </span>
                    </v-btn>
                    <div v-if="invited" class="invitationButtons d-inline">
                      <div class="acceptToConnectButtonParent">
                        <v-btn
                          :loading="sendingAction"
                          :disabled="sendingAction"
                          class="btn btn-primary mx-auto acceptToConnectButton"
                          @click="acceptToConnect">
                          <i class="uiIconSocConnectUser d-none d-sm-inline"/>
                          <span class="d-none d-sm-flex">
                            {{ $t('profileHeader.button.acceptToConnect') }}
                          </span>
                          <v-icon class="d-inline d-sm-none">mdi-close</v-icon>
                        </v-btn>
                        <v-btn
                          class="btn btn-primary peopleButtonMenu d-none d-sm-inline"
                          @click="openSecondButton">
                          <v-icon>mdi-menu-down</v-icon>
                        </v-btn>
                      </div>
                      <v-btn
                        v-show="displaySecondButton"
                        :loading="sendingSecondAction"
                        :disabled="sendingSecondAction"
                        class="btn mx-auto refuseToConnectButton"
                        @click="refuseToConnect">
                        <i class="uiIconSocCancelConnectUser d-none d-sm-inline"/>
                        <span class="d-none d-sm-flex">
                          {{ $t('profileHeader.button.refuseToConnect') }}
                        </span>
                        <v-icon class="d-inline d-sm-none">mdi-close</v-icon>
                      </v-btn>
                    </div>
                    <v-btn
                      v-else-if="requested"
                      :loading="sendingAction"
                      :disabled="sendingAction"
                      class="btn btn-primary mx-auto cancelRequestButton"
                      @click="cancelRequest">
                      <i class="uiIconSocCancelConnectUser d-none d-sm-inline"/>
                      <span class="d-none d-sm-inline">
                        {{ $t('profileHeader.button.cancelRequest') }}
                      </span>
                      <v-icon class="d-inline d-sm-none">mdi-close</v-icon>
                    </v-btn>
                    <v-btn
                      v-else-if="disconnected"
                      :class="skeleton && 'skeleton-background skeleton-text'"
                      :loading="sendingAction"
                      :disabled="sendingAction || skeleton"
                      class="btn btn-primary mx-auto connectUserButton"
                      @click="connect">
                      <i class="uiIconSocConnectUser d-none d-sm-inline"/>
                      <span class="d-none d-sm-inline">
                        {{ skeleton && '&nbsp;' || $t('profileHeader.button.connect') }}
                      </span>
                      <v-icon class="d-inline d-sm-none">mdi-plus</v-icon>
                    </v-btn>
                  </template>
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
import * as userService from '../../common/js/UserService.js'; 
import {upload} from '../../common/js/UploadService.js'; 

export default {
  props: {
    maxUploadSize: {
      type: Number,
      default: () => 2,
    },
  },
  data: () => ({
    skeleton: true,
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    file: null,
    user: null,
    avatarExcceedsLimitError: 'AVATAR_EXCEEDS_LIMIT',
    bannerExcceedsLimitError: 'BANNER_EXCEEDS_LIMIT',
    profileActionExtensions: [],
    sendingAction: false,
    sendingSecondAction: false,
    sendingImage: false,
    displaySecondButton: false,
    errorMessage: null,
    waitTimeUntilCloseMenu: 200,
  }),
  computed: {
    maxUploadSizeInBytes() {
      return this.maxUploadSize * 1024 * 1024;
    },
    relationshipStatus() {
      return this.user && this.user.relationshipStatus;
    },
    connected() {
      return this.relationshipStatus === 'CONFIRMED';
    },
    disconnected() {
      return !this.relationshipStatus || this.relationshipStatus === 'IGNORED';
    },
    invited() {
      return this.relationshipStatus === 'INCOMING';
    },
    requested() {
      return this.relationshipStatus === 'OUTGOING';
    },
    enabledProfileActionExtensions() {
      if (!this.profileActionExtensions || !this.user) {
        return [];
      }
      return this.profileActionExtensions.slice().filter(extension => extension.enabled(this.user));
    },
  },
  watch: {
    sendingImage() {
      if (this.sendingImage) {
        document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      } else {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      }
    },
  },
  created() {
    // To refresh menu when a new extension is ready to be used
    document.addEventListener('profile-extension-updated', this.refreshExtensions);

    // To broadcast event about current page supporting profile extensions
    document.dispatchEvent(new CustomEvent('profile-extension-init'));

    this.refreshExtensions();

    $(document).on('mousedown', () => {
      if (this.displaySecondButton) {
        window.setTimeout(() => {
          this.displaySecondButton = false;
        }, this.waitTimeUntilCloseMenu);
      }
    });
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
    refreshExtensions() {
      this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
    },
    uploadAvatar(file) {
      if (file && file.size) {
        if (file.size > this.maxUploadSizeInBytes) {
          this.handleError(this.avatarExcceedsLimitError);
          return;
        }
        this.sendingImage = true;
        return upload(file)
          .then(uploadId => userService.updateProfileField('avatar', uploadId))
          .then(this.refresh)
          .catch(this.handleError)
          .finally(() => {
            this.sendingImage = false;
          });
      }
    },
    uploadBanner(file) {
      if (file && file.size) {
        if (file.size > this.maxUploadSizeInBytes) {
          this.handleError(this.bannerExcceedsLimitError);
          return;
        }
        this.sendingImage = true;
        return upload(file)
          .then(uploadId => userService.updateProfileField('banner', uploadId))
          .then(this.refresh)
          .catch(this.handleError)
          .finally(() => {
            this.sendingImage = false;
          });
      }
    },
    handleError(error) {
      if (error) {
        if (String(error).indexOf(this.avatarExcceedsLimitError) >= 0) {
          this.errorMessage = this.$t('profileHeader.label.avatarExcceededAllowedSize', {0: this.maxUploadSize});
        } else if (String(error).indexOf(this.bannerExcceedsLimitError) >= 0) {
          this.errorMessage = this.$t('profileHeader.label.bannerExcceededAllowedSize', {0: this.maxUploadSize});
        } else {
          this.errorMessage = String(error);
        }
        this.$refs.errorDialog.open();
      }
    },
    openSecondButton() {
      this.displaySecondButton = !this.displaySecondButton;
    },
    connect() {
      this.sendingAction = true;
      userService.connect(this.user.username)
        .then(() => this.refresh())
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    acceptToConnect() {
      this.sendingAction = true;
      userService.confirm(this.user.username)
        .then(() => this.refresh())
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    refuseToConnect() {
      this.sendingSecondAction = true;
      userService.deleteRelationship(this.user.username)
        .then(() => this.refresh())
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingSecondAction = false;
        });
    },
    cancelRequest() {
      this.sendingAction = true;
      userService.deleteRelationship(this.user.username)
        .then(() => this.refresh())
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
  },
};
</script>