<template>
  <v-card
    :id="userMenuParentId"
    class="peopleCardItem d-block d-sm-flex"
    flat>
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
          :lazy-src="`${userAvatarUrl}&size=65x65`"
          :src="`${userAvatarUrl}&size=65x65`"
          transition="none"
          class="mx-auto"
          height="65px"
          width="65px"
          max-height="65px"
          max-width="65px"
          eager />
      </a>
    </div>

    <v-card-text class="peopleCardBody align-center pt-2 pb-1">
      <a
        :href="url"
        :title="user.fullname"
        :class="(!user.enabled || user.deleted) && 'text-sub-title' || 'text-color'"
        class="userFullname text-truncate font-weight-bold d-block">
        {{ user.fullname }}
        <span v-if="user.external == 'true' " class="externalFlagClass">
          {{ $t('peopleList.label.external') }}
        </span>
      </a>
      <v-card-subtitle class="userPositionLabel text-truncate py-0">
        {{ user.position || '&nbsp;' }}
      </v-card-subtitle>
    </v-card-text>

    <v-card-actions v-if="!isSameUser" class="peopleCardActions">
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
        v-else-if="user.relationshipStatus === 'CONFIRMED'"
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
      <div v-else-if="user.relationshipStatus === 'INCOMING'" class="invitationButtons">
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
        v-else-if="user.relationshipStatus === 'OUTGOING'"
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
    publisherRolePromotionFeatureEnabled: false
  }),
  computed: {
    isSameUser() {
      return this.user && this.user.username === eXo.env.portal.userName;
    },
    userAvatarUrl() {
      return this.user && this.user.avatar || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.user.username}/avatar`;
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
            || (extension.title === this.$t('peopleList.button.setAsRedactor') || extension.title === this.$t('peopleList.button.removeRedactor') || extension.title === this.$t('peopleList.button.promotePublisher')) && (extension.enabled(this.user))));
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
    this.$featureService.isFeatureEnabled('publisherRolePromotion')
      .then(enabled => this.publisherRolePromotionFeatureEnabled = enabled);
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
  },
};
</script>
