<template>
  <v-card :id="spaceMenuParentId" class="spaceCardItem">

    <v-img
      :src="spaceBannerUrl"
      height="80px"
      class="white--text align-start d-none d-sm-block">
    </v-img>

    <div class="spaceAvatarImg">
      <a :href="url">
        <v-img
          :src="spaceAvatarUrl"
          class="mx-auto mt-3"
          height="50px"
          width="50px"
          max-height="50px"
          max-width="50px">
        </v-img>
      </a>
    </div>

    <div class="spaceToolbarIcons pa-2">
      <v-btn
        icon
        small
        icon
        class="spaceInfoIcon d-none d-sm-flex"
        @click="$emit('flip')">
        <v-icon small>fa-info</v-icon>
      </v-btn>
      <v-spacer />
      <template v-if="canUseActionsMenu">
        <v-btn icon depressed class="spaceActionIcon" @click="displayActionMenu = true">
          <v-icon>mdi-dots-vertical</v-icon>
        </v-btn>
        <v-menu
          ref="actionMenu"
          v-model="displayActionMenu"
          :attach="`#${spaceMenuParentId}`"
          transition="slide-x-reverse-transition"
          content-class="spaceActionMenu"
          offset-y>
          <v-list class="pa-0" dense>
            <template v-if="space.canEdit">
              <v-list-item @click="editSpace">
                <v-list-item-title class="subtitle-2">{{ $t('spacesList.button.edit') }}</v-list-item-title>
              </v-list-item>
              <v-list-item @click="removeSpaceConfirm">
                <v-list-item-title class="subtitle-2">{{ $t('spacesList.button.remove') }}</v-list-item-title>
              </v-list-item>
            </template>
            <v-list-item
              v-for="(extension, i) in enabledProfileActionExtensions"
              :key="i"
              @click="extension.click(space)">
              <v-list-item-title>{{ extension.title }}</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </template>
    </div>

    <v-card-text class="spaceCardBody align-center">
      <a :href="url" :title="space.displayName" class="spaceDisplayName text-truncate">{{ space.displayName }}</a>
      <v-card-subtitle class="pb-0">{{ $t('spacesList.label.members', {0: space.membersCount}) }}</v-card-subtitle>
    </v-card-text>

    <v-card-actions class="spaceCardActions">
      <exo-confirm-dialog
        ref="confirmDialog"
        :title="confirmTitle"
        :message="confirmMessage"
        :ok-label="$t('spacesList.label.ok')"
        :cancel-label="okMethod && $t('spacesList.label.cancel')"
        @ok="okConfirmDialog"
        @dialog-closed="closeConfirmDialog" />
      <v-btn
        v-if="space.isMember"
        :loading="sendingAction"
        :disabled="sendingAction"
        class="btn mx-auto leaveSpaceButton"
        depressed
        block
        @click="leaveConfirm">
        <v-icon>mdi-minus</v-icon>
        <span class="d-none d-sm-inline">
          {{ $t('spacesList.button.leave') }}
        </span>
      </v-btn>
      <div v-else-if="space.isInvited" class="invitationButtons">
        <div class="acceptToJoinSpaceButtonParent">
          <v-btn
            :loading="sendingAction"
            :disabled="sendingAction"
            class="btn mx-auto acceptToJoinSpaceButton"
            depressed
            @click="acceptToJoin">
            <v-icon>mdi-check</v-icon>
            <span class="d-none d-sm-flex">
              {{ $t('spacesList.button.acceptToJoin') }}
            </span>
          </v-btn>
          <v-btn
            class="btn spaceButtonMenu d-none d-sm-inline"
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
          class="btn mx-auto refuseToJoinSpaceButton"
          depressed
          block
          @click="refuseToJoin">
          <v-icon>mdi-close</v-icon>
          <span class="d-none d-sm-flex">
            {{ $t('spacesList.button.refuseToJoin') }}
          </span>
        </v-btn>
      </div>
      <v-btn
        v-else-if="space.isPending"
        :loading="sendingAction"
        :disabled="sendingAction"
        class="btn mx-auto cancelRequestToJoinSpaceButton"
        depressed
        block
        @click="cancelRequest">
        <v-icon>mdi-close</v-icon>
        <span class="d-none d-sm-inline">
          {{ $t('spacesList.button.cancelRequest') }}
        </span>
      </v-btn>
      <v-btn
        v-else-if="space.subscription === 'open'"
        :loading="sendingAction"
        :disabled="sendingAction"
        class="btn mx-auto joinSpaceButton"
        depressed
        block
        @click="join">
        <v-icon>mdi-plus</v-icon>
        <span class="d-none d-sm-inline">
          {{ $t('spacesList.button.join') }}
        </span>
      </v-btn>
      <v-btn
        v-else-if="space.subscription === 'validation'"
        :loading="sendingAction"
        :disabled="sendingAction"
        class="btn mx-auto joinSpaceButton"
        depressed
        block
        @click="requestJoin">
        <v-icon>mdi-plus</v-icon>
        <span class="d-none d-sm-inline">
          {{ $t('spacesList.button.requestJoin') }}
        </span>
      </v-btn>
      <div
        v-else
        :title="$t('spacesList.label.closedSpace')"
        class="joinSpaceDisabledLabel">
        <v-btn
          disabled
          class="btn mx-auto joinSpaceButton"
          depressed
          block
          @click="join">
          <v-icon>mdi-plus</v-icon>
          <span class="d-none d-sm-inline">
            {{ $t('spacesList.button.join') }}
          </span>
        </v-btn>
      </div>
    </v-card-actions>
  </v-card>
</template>

<script>
import * as spaceService from '../js/SpaceService.js'; 

export default {
  props: {
    space: {
      type: Object,
      default: null,
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
    spaceAvatarUrl() {
      return this.space && (this.space.avatarUrl || `/portal/rest/v1/social/spaces/${this.space.prettyName}/avatar`);
    },
    spaceBannerUrl() {
      return this.space && (this.space.bannerUrl || `/portal/rest/v1/social/spaces/${this.space.prettyName}/banner`);
    },
    spaceMenuParentId() {
      return this.space && this.space.id && `spaceMenuParent-${this.space.id}` || 'spaceMenuParent';
    },
    enabledProfileActionExtensions() {
      if (!this.profileActionExtensions || !this.space || !this.space.isMember) {
        return [];
      }
      return this.profileActionExtensions.slice().filter(extension => extension.enabled(this.space));
    },
    canUseActionsMenu() {
      return this.space && (this.space.canEdit || this.enabledProfileActionExtensions.length);
    },
    url() {
      if (this.space && this.space.groupId) {
        const uriPart = this.space.groupId.replace(/\//g, ':');
        return `${eXo.env.portal.context}/g/${uriPart}/`;
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
    editSpace() {
      this.$root.$emit('editSpace', this.space);
    },
    removeSpaceConfirm() {
      this.openConfirmDialog(
        this.$t('spacesList.title.deleteSpace'),
        this.$t('spacesList.message.deleteSpace'),
        this.removeSpace);
    },
    removeSpace() {
      this.sendingAction = true;
      spaceService.removeSpace(this.space.id)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    leaveConfirm() {
      const isOnlyManagrLeftInSpace = this.space.isManager && this.space.managersCount <= 1;
      if (isOnlyManagrLeftInSpace) {
        this.openConfirmDialog(
          this.$t('spacesList.warning'),
          this.$t('spacesList.warning.lastManager'));
      } else {
        this.openConfirmDialog(
          this.$t('spacesList.title.leaveSpace'),
          this.$t('spacesList.message.leaveSpace', {0: `<b>${this.space.displayName}</b>`}),
          this.leave);
      }
    },
    leave() {
      this.sendingAction = true;
      spaceService.leave(this.space.id)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    acceptToJoin() {
      this.sendingAction = true;
      spaceService.accept(this.space.id)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    refuseToJoin() {
      this.sendingSecondAction = true;
      spaceService.deny(this.space.id)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingSecondAction = false;
        });
    },
    join() {
      this.sendingAction = true;
      spaceService.join(this.space.id)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    requestJoin() {
      this.sendingAction = true;
      spaceService.requestJoin(this.space.id)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    cancelRequest() {
      this.sendingAction = true;
      spaceService.cancel(this.space.id)
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

