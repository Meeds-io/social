<template>
  <v-hover v-slot="{ hover }">
    <v-card
      :id="spaceMenuParentId"
      :elevation="hover && !isMobile && 4 || 0"
      class="spaceCardItem d-block d-sm-flex mx-2"
      :outlined="!isMobile">
      <v-img
        :src="spaceBannerUrl"
        transition="none"
        height="80px"
        min-height="80px"
        max-height="80px"
        min-width="100%"
        class="white--text align-start d-block spaceBannerImg"
        eager />

      <div class="spaceToolbarIcons px-2">
        <v-btn
          :title="$t('spaceList.label.openSpaceInfo')"
          icon
          small
          class="spaceInfoIcon d-flex"
          @click="$emit('flip')">
          <v-icon size="12">fa-info</v-icon>
        </v-btn>
        <v-spacer />
        <exo-space-favorite-action
          v-if="space.isMember && !isMobile"
          :is-favorite="space.isFavorite"
          :space-id="space.id" />
        <div v-if="isMobile">
          <v-icon 
            size="14" 
            class="my-3" 
            @click="openBottomMenu">
            fa-ellipsis-v
          </v-icon>
          <v-bottom-sheet v-model="bottomMenu" class="pa-0">
            <v-sheet class="text-center" height="150px">
              <v-list dense>
                <v-list-item 
                  v-if="space.isMember"
                  @click="leave">
                  <v-list-item-title class="align-center d-flex">
                    <v-icon class="mx-4" size="16">fas fa-minus-circle</v-icon>
                    <span class="mx-2">
                      {{ $t('spacesList.button.leave') }}
                    </span>
                  </v-list-item-title>
                </v-list-item>
                <v-list-item 
                  v-else-if="space.isInvited">
                  <v-list-item-title class="align-center d-flex">
                    <div @click="acceptToJoin">
                      <v-icon class="mx-4" size="16">mdi-check</v-icon>
                      <span class="mx-2">
                        {{ $t('spacesList.button.acceptToJoin') }}
                      </span>
                    </div>
                    <v-divider
                      vertical />
                    <div @click="refuseToJoin">
                      <v-icon class="mx-4" size="16">mdi-close</v-icon>
                      <span class="mx-2">
                        {{ $t('spacesList.button.refuseToJoin') }}
                      </span>
                    </div>
                  </v-list-item-title>
                </v-list-item>
                <v-list-item 
                  v-else-if="space.subscription === 'open' && !space.isMember"
                  @click="join">
                  <v-list-item-title class="align-center d-flex">
                    <v-icon class="mx-4" size="16">fas fa-plus-circle</v-icon>
                    <span class="mx-2">
                      {{ $t('spacesList.button.join') }}
                    </span>
                  </v-list-item-title>
                </v-list-item>
                <v-list-item 
                  v-else-if="space.subscription === 'validation' && !space.isMember && !space.isPending"
                  @click="requestJoin">
                  <v-list-item-title class="align-center d-flex">
                    <v-icon class="mx-4" size="16">fas fa-plus-circle</v-icon>
                    <span class="mx-2">
                      {{ $t('spacesList.button.requestJoin') }}
                    </span>
                  </v-list-item-title>
                </v-list-item>
                <v-list-item 
                  v-if="space.isPending"
                  @click="cancelRequest">
                  <v-list-item-title class="align-center d-flex">
                    <v-icon class="mx-4" size="16">fas fa-minus-circle</v-icon>
                    <span class="mx-2">
                      {{ $t('spacesList.button.cancelRequest') }}
                    </span>
                  </v-list-item-title>
                </v-list-item>
                <v-list-item 
                  v-if="space.isManager"
                  @click="editSpace">
                  <v-list-item-title class="align-center d-flex">
                    <v-icon class="mx-4" size="16">fa-edit</v-icon>
                    <span class="mx-2">
                      {{ $t('spacesList.button.edit') }}
                    </span>
                  </v-list-item-title>
                </v-list-item>
                <v-list-item v-if="space.isMember">
                  <v-list-item-title>
                    <exo-space-favorite-action
                      :is-favorite="space.isFavorite"
                      :space-id="space.id"
                      extra-class="ms-3"
                      display-label />
                  </v-list-item-title>
                </v-list-item>
              </v-list>
            </v-sheet>
          </v-bottom-sheet>
        </div>
        <template v-if="canUseActionsMenu">
          <v-btn
            :title="$t('spaceList.label.openSpaceMenu')"
            icon
            text
            class="spaceMenuIcon d-block"
            @click="displayActionMenu = true">
            <v-icon size="21">mdi-dots-vertical</v-icon>
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
                  <v-list-item-title class="subtitle-2">
                    <i class="uiIcon uiIconEdit"></i>
                    {{ $t('spacesList.button.edit') }}
                  </v-list-item-title>
                </v-list-item>
                <v-list-item @click="removeSpaceConfirm">
                  <v-list-item-title class="subtitle-2">
                    <i class="uiIcon uiIconTrash"></i>
                    {{ $t('spacesList.button.remove') }}
                  </v-list-item-title>
                </v-list-item>
              </template>
              <v-list-item
                v-for="(extension, i) in enabledProfileActionExtensions"
                :key="i"
                @click="extension.click(space)">
                <v-list-item-title>
                  <i :class="extension.icon ? extension.icon : 'hidden'" class="uiIcon "></i>
                  {{ extension.title }}
                </v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
        </template>
      </div>

      <div class="spaceAvatar">
        <a :href="url">
          <v-img
            :src="`${spaceAvatarUrl}&size=75x75`"
            transition="none"
            class="mx-auto"
            height="75px"
            width="75px"
            max-height="75px"
            max-width="75px"
            eager />
        </a>
      </div>

      <v-card-text
        class="spaceCardBody align-center py-sm-2">
        <a
          :href="url"
          :title="space.displayName"
          :class="isMobile && 'text-truncate-2 mt-0' || 'text-truncate d-block'"
          class="spaceDisplayName">
          {{ space.displayName }}
        </a>
        <a 
          :href="url"
          class="spaceMembersLabel py-0 my-0 my-sm-auto">
          {{ $t('spacesList.label.members', {0: space.membersCount}) }}
        </a>
      </v-card-text>
      <v-card-actions v-if="!isMobile" class="spaceCardActions">
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
          :disabled="sendingAction || space.isUserBound"
          class="btn mx-auto spaceMembershipButton leaveSpaceButton"
          depressed
          block
          @click="leaveConfirm">
          <v-icon>mdi-minus</v-icon>
          <span class="spaceMembershipButtonText d-inline">
            {{ $t('spacesList.button.leave') }}
          </span>
        </v-btn>
        <div v-else-if="space.isInvited" class="invitationButtons">
          <div class="acceptToJoinSpaceButtonParent">
            <v-btn
              :loading="sendingAction"
              :disabled="sendingAction"
              class="btn mx-auto spaceMembershipButton acceptToJoinSpaceButton"
              depressed
              @click="acceptToJoin">
              <v-icon>mdi-check</v-icon>
              <span class="d-flex">
                {{ $t('spacesList.button.acceptToJoin') }}
              </span>
            </v-btn>
            <v-btn
              class="btn spaceButtonMenu d-inline"
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
            class="btn mx-auto spaceMembershipButton refuseToJoinSpaceButton"
            depressed
            block
            @click="refuseToJoin">
            <v-icon>mdi-close</v-icon>
            <span class="d-flex">
              {{ $t('spacesList.button.refuseToJoin') }}
            </span>
          </v-btn>
        </div>
        <v-btn
          v-else-if="space.isPending"
          :loading="sendingAction"
          :disabled="sendingAction"
          class="btn mx-auto spaceMembershipButton cancelRequestToJoinSpaceButton"
          depressed
          block
          @click="cancelRequest">
          <v-icon>mdi-close</v-icon>
          <span class="spaceMembershipButtonText d-inline">
            {{ $t('spacesList.button.cancelRequest') }}
          </span>
        </v-btn>
        <v-btn
          v-else-if="space.subscription === 'open'"
          :loading="sendingAction"
          :disabled="sendingAction"
          class="btn mx-auto spaceMembershipButton joinSpaceButton"
          depressed
          block
          @click="join">
          <v-icon>mdi-plus</v-icon>
          <span class="spaceMembershipButtonText d-inline">
            {{ $t('spacesList.button.join') }}
          </span>
        </v-btn>
        <v-btn
          v-else-if="space.subscription === 'validation'"
          :loading="sendingAction"
          :disabled="sendingAction"
          class="btn mx-auto spaceMembershipButton joinSpaceButton"
          depressed
          block
          @click="requestJoin">
          <v-icon>mdi-plus</v-icon>
          <span class="spaceMembershipButtonText d-inline">
            {{ $t('spacesList.button.requestJoin') }}
          </span>
        </v-btn>
        <div
          v-else
          :title="$t('spacesList.label.closedSpace')"
          class="joinSpaceDisabledLabel">
          <v-btn
            disabled
            class="btn mx-auto spaceMembershipButton joinSpaceButton"
            depressed
            block
            @click="join">
            <v-icon>mdi-plus</v-icon>
            <span class="spaceMembershipButtonText d-inline">
              {{ $t('spacesList.button.join') }}
            </span>
          </v-btn>
        </div>
      </v-card-actions>
    </v-card>
  </v-hover>
</template>

<script>
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
    bottomMenu: false,
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
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    },
    isFavorite() {
      return this.space?.isFavorite === 'true';
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
      this.$spaceService.removeSpace(this.space.id)
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
      this.$spaceService.leave(this.space.id)
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
      this.$spaceService.accept(this.space.id)
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
      this.$spaceService.deny(this.space.id)
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
      this.$spaceService.join(this.space.id)
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
      this.$spaceService.requestJoin(this.space.id)
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
      this.$spaceService.cancel(this.space.id)
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
      this.bottomMenu = true;
    },
  },
};
</script>

