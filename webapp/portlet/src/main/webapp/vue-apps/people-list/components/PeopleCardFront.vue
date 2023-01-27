<template>
  <v-card
    :id="userMenuParentId"
    :outlined="!isMobile"
    class="peopleCardItem d-block d-sm-flex"
    flat
    hover>
    <v-img
      :lazy-src="userBannerUrl"
      :src="userBannerUrl"
      transition="none"
      height="80px"
      class="white--text align-start d-block peopleBannerImg"
      eager />

    <div class="peopleToolbarIcons px-2">
      <v-btn
        :title="$t('peopleList.label.openUserInfo')"
        icon
        small
        class="peopleInfoIcon d-flex"
        @click="$emit('flip')">
        <v-icon size="12">fa-info</v-icon>
      </v-btn>
      <v-btn
        v-if="user.isManager"
        :title="$t('peopleList.label.spaceManager')"
        :ripple="false"
        color="primary"
        class="peopleInfoIcon d-flex not-clickable primary-border-color ms-1"
        icon
        small>
        <v-icon size="12">mdi-account-cog</v-icon>
      </v-btn>
      <v-btn
        v-if="user.isSpaceRedactor"
        :title="$t('peopleList.label.spaceRedactor')"
        :ripple="false"
        color="primary"
        class="peopleInfoIcon d-flex not-clickable primary-border-color ms-1"
        icon
        small>
        <v-icon size="12">mdi-account-edit</v-icon>
      </v-btn>
      <v-btn
        v-if="publisherRolePromotionFeatureEnabled && user.isSpacePublisher"
        :title="$t('peopleList.label.spacePublisher')"
        :ripple="false"
        color="primary"
        class="peopleInfoIcon d-flex not-clickable primary-border-color ms-1"
        icon
        small>
        <v-icon size="12">fa-paper-plane</v-icon>
      </v-btn>
      <v-btn
        v-if="user.isGroupBound"
        :title="$t('peopleList.label.groupBound')"
        :ripple="false"
        color="grey"
        class="peopleGroupMemberBindingIcon d-flex not-clickable ms-1"
        icon
        small>
        <span class="d-flex uiIconPLFFont uiIconGroup"></span>
      </v-btn>
      <v-spacer />
      <div v-if="isMobile && !isSameUser">
        <v-icon 
          size="14" 
          class="my-3" 
          @click="openBottomMenu">
          fas fa-ellipsis-v
        </v-icon>
        <v-bottom-sheet v-model="bottomMenu" class="pa-0">
          <v-sheet class="text-center" height="100">
            <v-list dense>
              <v-list-item 
                v-if="confirmedUser"
                @click="disconnect">
                <v-list-item-title class="align-center d-flex">
                  <v-icon class="mx-4" size="16">fas fa-minus-circle</v-icon>
                  <span class="mx-2">
                    {{ $t('peopleList.button.disconnect') }}
                  </span>
                </v-list-item-title>
              </v-list-item>
              <v-list-item 
                v-else-if="incomingUser">
                <v-list-item-title class="align-center d-flex">
                  <div @click="acceptToConnect">
                    <v-icon class="mx-4" size="16">mdi-check</v-icon>
                    <span class="mx-2">
                      {{ $t('peopleList.button.acceptToConnect') }}
                    </span>
                  </div>
                  <v-divider
                    vertical />
                  <div @click="refuseToConnect">
                    <v-icon class="mx-4" size="16">mdi-close</v-icon>
                    <span class="mx-2">
                      {{ $t('peopleList.button.refuseToConnect') }}
                    </span>
                  </div>
                </v-list-item-title>
              </v-list-item>
              <v-list-item 
                v-else-if="outgoingUser"
                @click="cancelRequest">
                <v-list-item-title class="align-center d-flex">
                  <v-icon class="mx-4" size="16">mdi-close</v-icon>
                  <span class="mx-2">
                    {{ $t('peopleList.button.cancelRequest') }}
                  </span>
                </v-list-item-title>
              </v-list-item>
              <v-list-item 
                v-else
                @click="connect">
                <v-list-item-title class="align-center d-flex">
                  <v-icon class="mx-4" size="16">fas fa-plus-circle</v-icon>
                  <span class="mx-2">
                    {{ $t('peopleList.button.connect') }}
                  </span>
                </v-list-item-title>
              </v-list-item>
              <v-list-item
                v-for="(extension, i) in enabledProfileActionExtensions"
                :key="i"
                @click="extensionClick(extension)">
                <v-list-item-title class="align-center d-flex">
                  <v-icon class="mx-4" size="20">{{ extension.class }}</v-icon>
                  <span class="mx-2">
                    {{ extension.title }}
                  </span>
                </v-list-item-title>
              </v-list-item>
            </v-list>
          </v-sheet>
        </v-bottom-sheet>
      </div>
      <template v-if="canUseActionsMenu">
        <v-btn
          :title="$t('peopleList.label.openUserMenu')"
          icon
          text
          class="peopleMenuIcon d-block"
          @click="displayActionMenu = true">
          <v-icon size="21">mdi-dots-vertical</v-icon>
        </v-btn>
        <v-menu
          ref="actionMenu"
          v-model="displayActionMenu"
          :attach="`#${userMenuParentId}`"
          transition="slide-x-reverse-transition"
          content-class="peopleActionMenu"
          offset-y>
          <v-list class="pa-0" dense>
            <v-list-item
              v-for="(extension, i) in enabledProfileActionExtensions"
              :key="i"
              @click="extension.click(user)">
              <v-list-item-title class="peopleActionItem">
                <i :class="extension.icon ? extension.icon : 'hidden'" class="uiIcon"></i>
                {{ extension.title }}
              </v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </template>
    </div>

    <div class="peopleAvatar">
      <a :href="url">
        <v-img
          :lazy-src="`${userAvatarUrl}`"
          :src="`${userAvatarUrl}`"
          transition="none"
          class="mx-auto"
          height="65px"
          width="65px"
          max-height="65px"
          max-width="65px"
          eager />
      </a>
    </div>

    <v-card-text class="peopleCardBody align-center py-1">
      <a
        :href="url"
        :title="user.fullname"
        :class="usernameClass"
        class="userFullname font-weight-bold">
        {{ user.fullname }}
        <span v-if="user.external == 'true' " class="externalFlagClass">
          {{ $t('peopleList.label.external') }}
        </span>
      </a>
      <v-card-subtitle 
        class="userPositionLabel text-truncate py-0 mt-0 mt-sm-auto">
        {{ user.position || '&nbsp;' }}
      </v-card-subtitle>
    </v-card-text>

    <v-card-actions v-if="!isSameUser && !isMobile" class="peopleCardActions">
      <exo-confirm-dialog
        ref="confirmDialog"
        :title="confirmTitle"
        :message="confirmMessage"
        :ok-label="$t('peopleList.label.ok')"
        :cancel-label="okMethod && $t('peopleList.label.cancel')"
        @ok="okConfirmDialog"
        @dialog-closed="closeConfirmDialog" />
      <v-btn
        v-if="!user.enabled || user.deleted"
        class="btn mx-auto cancelRequestButton"
        depressed
        disabled
        block>
        <span v-if="user.deleted" class="d-inline peopleDeleteButton">
          {{ $t('peopleList.label.deletedUser') }}
        </span>
        <span v-else class="d-inline peopleDeleteButton">
          {{ $t('peopleList.label.disabledUser') }}
        </span>
        <v-icon class="d-none peopleDeleteButtonMinus">mdi-minus</v-icon>
      </v-btn>
      <v-btn
        v-else-if="confirmedUser"
        :loading="sendingAction"
        :disabled="sendingAction"
        class="btn mx-auto peopleRelationshipButton disconnectUserButton"
        depressed
        block
        @click="disconnectConfirm">
        <i class="uiIconSocCancelConnectUser peopleRelationshipIcon d-inline"></i>
        <span class="d-inline peopleRelationshipButtonText">
          {{ $t('peopleList.button.disconnect') }}
        </span>
        <v-icon class="d-none relationshipButtonMinus">mdi-minus</v-icon>
      </v-btn>
      <div v-else-if="incomingUser" class="invitationButtons">
        <div class="acceptToConnectButtonParent">
          <v-btn
            :loading="sendingAction"
            :disabled="sendingAction"
            class="btn mx-auto peopleRelationshipButton acceptToConnectButton"
            depressed
            @click="acceptToConnect">
            <i class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
            <span class="d-flex">
              {{ $t('peopleList.button.acceptToConnect') }}
            </span>
            <v-icon class="d-none relationshipButtonMinus">mdi-check</v-icon>
          </v-btn>
          <v-btn
            class="btn peopleButtonMenu d-inline"
            depressed
            x-small
            @click="displaySecondButton = !displaySecondButton">
            <v-icon>mdi-menu-down</v-icon>
          </v-btn>
        </div>
        <v-btn
          v-show="displaySecondButton"
          :loading="sendingSecondAction"
          :disabled="sendingSecondAction"
          class="btn mx-auto peopleRelationshipButton refuseToConnectButton"
          depressed
          block
          @click="refuseToConnect">
          <i class="uiIconSocCancelConnectUser peopleRelationshipIcon d-inline"></i>
          <span class="d-flex">
            {{ $t('peopleList.button.refuseToConnect') }}
          </span>
          <v-icon class="d-none relationshipButtonMinus">mdi-close</v-icon>
        </v-btn>
      </div>
      <v-btn
        v-else-if="outgoingUser"
        :loading="sendingAction"
        :disabled="sendingAction"
        class="btn mx-auto peopleRelationshipButton cancelRequestButton"
        depressed
        block
        @click="cancelRequest">
        <i class="uiIconSocCancelConnectUser peopleRelationshipIcon d-inline"></i>
        <span class="d-inline peopleRelationshipButtonText">
          {{ $t('peopleList.button.cancelRequest') }}
        </span>
        <v-icon class="d-none relationshipButtonMinus">mdi-close</v-icon>
      </v-btn>
      <v-btn
        v-else
        :loading="sendingAction"
        :disabled="sendingAction"
        class="btn mx-auto peopleRelationshipButton connectUserButton"
        depressed
        block
        @click="connect">
        <i class="uiIconSocConnectUser peopleRelationshipIcon d-inline"></i>
        <span class="d-inline peopleRelationshipButtonText">
          {{ $t('peopleList.button.connect') }}
        </span>
        <v-icon class="d-none relationshipButtonMinus">mdi-plus</v-icon>
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: null,
    },
    isManager: {
      type: Boolean,
      default: false,
    },
    profileActionExtensions: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    displayActionMenu: false,
    waitTimeUntilCloseMenu: 200,
    sendingAction: false,
    sendingSecondAction: false,
    confirmTitle: '',
    confirmMessage: '',
    okMethod: null,
    displaySecondButton: false,
    publisherRolePromotionFeatureEnabled: eXo.env.portal.PublisherRolePromotionFeatureEnabled,
    bottomMenu: false
  }),
  computed: {
    isSameUser() {
      return this.user && this.user.username === eXo.env.portal.userName;
    },
    userAvatarUrl() {
      let userAvatarUrl = this.user && this.user.avatar || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.user.username}/avatar`;
      if (!userAvatarUrl.includes('?')) {
        userAvatarUrl += '?';
      } else {
        userAvatarUrl += '&';
      }
      userAvatarUrl += 'size=65x65';
      return userAvatarUrl;
    },
    userBannerUrl() {
      return this.user && this.user.banner || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.user.username}/banner`;
    },
    userMenuParentId() {
      return this.user && this.user.id && `userMenuParent-${this.user.id}` || 'userMenuParent';
    },
    enabledProfileActionExtensions() {
      if (!this.profileActionExtensions || !this.user) {
        return [];
      }
      if (this.isSameUser && this.user.isManager) {
        return this.profileActionExtensions.slice().filter(extension => ((extension.title === this.$t('peopleList.button.removeManager'))
            || (extension.title === this.$t('peopleList.button.setAsRedactor') || extension.title === this.$t('peopleList.button.removeRedactor') || extension.title === this.$t('peopleList.button.promotePublisher') || extension.title === this.$t('peopleList.button.removePublisher')) && (extension.enabled(this.user))));
      }
      return this.profileActionExtensions.slice().filter(extension => extension.enabled(this.user));
    },
    canUseActionsMenu() {
      return this.user && this.enabledProfileActionExtensions.length;
    },
    url() {
      if (this.user && this.user.username) {
        return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.user.username}`;
      } else {
        return '#';
      }
    },
    isMobile() {
      return this.$vuetify.breakpoint.xs;
    },
    usernameClass() {
      return `${(!this.user.enabled || this.user.deleted) && 'text-sub-title' || 'text-color'} ${this.isMobile && 'text-truncate-2 mt-0' || 'text-truncate pt-1 d-block'}`;
    },
    confirmedUser() {
      return this.user?.relationshipStatus === 'CONFIRMED';
    },
    incomingUser() {
      return this.user?.relationshipStatus === 'INCOMING';
    },
    outgoingUser() {
      return this.user?.relationshipStatus === 'OUTGOING';
    }
  },
  created() {
    $(document).on('mousedown', () => {
      if (this.displayActionMenu) {
        window.setTimeout(() => {
          this.displayActionMenu = false;
          this.displaySecondButton = false;
        }, this.waitTimeUntilCloseMenu);
      }
    });
  },
  methods: {
    connect() {
      this.sendingAction = true;
      this.$userService.connect(this.user.username)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    disconnect() {
      this.sendingAction = true;
      this.$userService.deleteRelationship(this.user.username)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    disconnectConfirm() {
      this.openConfirmDialog(
        this.$t('peopleList.title.disconnectConfirm'),
        this.$t('peopleList.message.disconnectConfirm', {0: `<b>${this.user.fullname}</b>`}),
        this.disconnect);
    },
    acceptToConnect() {
      this.sendingAction = true;
      this.$userService.confirm(this.user.username)
        .then(() => this.$emit('refresh'))
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
      this.$userService.deleteRelationship(this.user.username)
        .then(() => this.$emit('refresh'))
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
      this.$userService.deleteRelationship(this.user.username)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    openConfirmDialog(title, message, okMethod) {
      this.confirmTitle = title;
      this.confirmMessage = message;
      if (okMethod) {
        this.okMethod = okMethod;
      } else {
        this.okMethod = null;
      }
      this.$refs.confirmDialog.open();
    },
    okConfirmDialog() {
      if (this.okMethod) {
        this.okMethod();
      }
    },
    closeConfirmDialog() {
      this.confirmTitle = '';
      this.confirmMessage = '';
      this.okMethod = null;
    },
    openBottomMenu() {
      if (!this.isSameUser) {
        this.bottomMenu = true;
      }
    },
    extensionClick(extension) {
      extension.click(this.user);
      this.bottomMenu = false;
    }
  },
};
</script>
