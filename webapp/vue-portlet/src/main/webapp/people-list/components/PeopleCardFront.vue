<template>
  <v-card :id="userMenuParentId" class="peopleCardItem d-block d-sm-flex" flat>
    <v-img
      :src="!skeleton && userBannerUrl || ''"
      height="80px"
      class="white--text align-start d-none d-sm-block peopleBannerImg">
    </v-img>

    <div class="peopleToolbarIcons px-2">
      <v-btn
        :disabled="skeleton"
        :class="skeleton && 'skeleton-background skeleton-text'"
        icon
        small
        class="peopleInfoIcon d-none d-sm-flex"
        @click="$emit('flip')">
        <v-icon size="12">fa-info</v-icon>
      </v-btn>
      <v-btn
        v-if="user.isManager"
        :title="$t('peopleList.label.spaceManager')"
        :ripple="false"
        color="primary"
        class="white primary-border-color ml-1 mt-2 not-clickable"
        icon
        x-small>
        <span class="d-none d-sm-flex uiIconMemberAdmin primary--text"></span>
      </v-btn>
      <v-spacer />
      <template v-if="skeleton || canUseActionsMenu">
        <v-btn
          :disabled="skeleton"
          :class="skeleton && 'skeleton-background skeleton-text'"
          icon
          text
          class="peopleMenuIcon d-none d-sm-block"
          @click="displayActionMenu = true">
          <v-icon size="21">mdi-dots-vertical</v-icon>
        </v-btn>
        <v-menu
          v-if="!skeleton"
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
                <i :class="extension.icon ? extension.icon : 'hidden'" class="uiIcon" />
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
          :src="!skeleton && userAvatarUrl || ''"
          :class="skeleton && 'skeleton-background'"
          class="mx-auto"
          height="65px"
          width="65px"
          max-height="65px"
          max-width="65px">
        </v-img>
      </a>
    </div>

    <v-card-text class="peopleCardBody align-center pt-2 pb-1">
      <a
        :href="url"
        :title="user.fullname"
        class="userFullname text-truncate text-color font-weight-bold d-block">
        {{ skeleton && '&nbsp;' || user.fullname }}
      </a>
      <v-card-subtitle
        class="userPositionLabel text-truncate py-0">
        {{ skeleton && '&nbsp;' || user.position || '&nbsp;' }}
      </v-card-subtitle>
    </v-card-text>

    <v-card-actions class="peopleCardActions">
      <exo-confirm-dialog
        ref="confirmDialog"
        :title="confirmTitle"
        :message="confirmMessage"
        :ok-label="$t('peopleList.label.ok')"
        :cancel-label="okMethod && $t('peopleList.label.cancel')"
        @ok="okConfirmDialog"
        @dialog-closed="closeConfirmDialog" />
      <v-btn
        v-if="user.relationshipStatus === 'CONFIRMED'"
        :loading="sendingAction"
        :disabled="sendingAction"
        class="btn mx-auto disconnectUserButton"
        depressed
        block
        @click="disconnectConfirm">
        <i class="uiIconSocCancelConnectUser d-none d-sm-inline"/>
        <span class="d-none d-sm-inline">
          {{ $t('peopleList.button.disconnect') }}
        </span>
        <v-icon class="d-inline d-sm-none">mdi-minus</v-icon>
      </v-btn>
      <div v-else-if="user.relationshipStatus === 'INCOMING'" class="invitationButtons">
        <div class="acceptToConnectButtonParent">
          <v-btn
            :loading="sendingAction"
            :disabled="sendingAction"
            class="btn mx-auto acceptToConnectButton"
            depressed
            @click="acceptToConnect">
            <i class="uiIconSocConnectUser d-none d-sm-inline"/>
            <span class="d-none d-sm-flex">
              {{ $t('peopleList.button.acceptToConnect') }}
            </span>
            <v-icon class="d-inline d-sm-none">mdi-check</v-icon>
          </v-btn>
          <v-btn
            class="btn peopleButtonMenu d-none d-sm-inline"
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
          class="btn mx-auto refuseToConnectButton"
          depressed
          block
          @click="refuseToConnect">
          <i class="uiIconSocCancelConnectUser d-none d-sm-inline"/>
          <span class="d-none d-sm-flex">
            {{ $t('peopleList.button.refuseToConnect') }}
          </span>
          <v-icon class="d-inline d-sm-none">mdi-close</v-icon>
        </v-btn>
      </div>
      <v-btn
        v-else-if="user.relationshipStatus === 'OUTGOING'"
        :loading="sendingAction"
        :disabled="sendingAction"
        class="btn mx-auto cancelRequestButton"
        depressed
        block
        @click="cancelRequest">
        <i class="uiIconSocCancelConnectUser d-none d-sm-inline"/>
        <span class="d-none d-sm-inline">
          {{ $t('peopleList.button.cancelRequest') }}
        </span>
        <v-icon class="d-inline d-sm-none">mdi-close</v-icon>
      </v-btn>
      <v-btn
        v-else
        :class="skeleton && 'skeleton-background skeleton-text'"
        :loading="sendingAction"
        :disabled="sendingAction || skeleton"
        class="btn mx-auto connectUserButton"
        depressed
        block
        @click="connect">
        <i class="uiIconSocConnectUser d-none d-sm-inline"/>
        <span class="d-none d-sm-inline">
          {{ skeleton && '&nbsp;' || $t('peopleList.button.connect') }}
        </span>
        <v-icon class="d-inline d-sm-none">mdi-plus</v-icon>
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
    skeleton: {
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
  }),
  computed: {
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
      return this.profileActionExtensions.slice().filter(extension => extension.enabled(this.user));
    },
    canUseActionsMenu() {
      return this.user && this.user.username !== eXo.env.portal.userName && this.enabledProfileActionExtensions.length;
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

