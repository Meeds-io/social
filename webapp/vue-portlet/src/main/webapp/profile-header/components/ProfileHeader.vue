<template>
  <v-app>
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
              <v-hover :disabled="skeleton">
                <v-avatar
                  slot-scope="{ hover }"
                  :class="skeleton && 'skeleton-background' || owner && hover && 'profileHeaderAvatarHoverEdit'"
                  class="align-start flex-grow-0 ml-3 my-3 profileHeaderAvatar"
                  size="165">
                  <v-img :src="!skeleton && user && user.avatar || ''" />
                  <v-btn
                    v-if="owner"
                    v-show="hover"
                    icon
                    absolute
                    bottom
                    outlined>
                    <i class="uiIconCamera uiIcon32x32 uiIconSocWhite" />
                  </v-btn>
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
                <v-btn
                  v-if="owner"
                  v-show="hover"
                  :depressed="false"
                  class="changeBannerButton mr-4"
                  icon
                  absolute
                  top>
                  <v-icon color="white" size="18">fa-file-image</v-icon>
                </v-btn>
                <div
                  id="profileHeaderActions"
                  :class="owner && 'profileHeaderOwnerActions' || 'profileHeaderOtherActions'"
                  class="mt-auto mr-3">
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
                  <template v-if="!owner">
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
                          <v-icon class="d-inline d-sm-none">mdi-check</v-icon>
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
import {getUser} from '../../common/js/UserService.js';

export default {
  data: () => ({
    skeleton: true,
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    user: null,
    profileActionExtensions: [],
    sendingAction: false,
    sendingSecondAction: false,
    displaySecondButton: false,
    waitTimeUntilCloseMenu: 200,
  }),
  computed: {
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
    getUser(eXo.env.portal.profileOwner, 'relationshipStatus')
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
  methods: {
    refreshExtensions() {
      this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
    },
    openSecondButton() {
      this.displaySecondButton = !this.displaySecondButton;
    },
  },
};
</script>