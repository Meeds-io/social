<template>
  <div>
    <component
      v-if="space.canEdit"
      :is="$root.isMobile && 'v-bottom-sheet' || 'v-menu'"
      ref="actionMenu"
      v-model="menu"
      :attach="$root.isMobile && '#vuetify-apps' || attachMenu"
      :left="!$vuetify.rtl"
      :right="$vuetify.rtl"
      transition="slide-x-reverse-transition"
      content-class="position-absolute application-menu z-index-modal"
      offset-y>
      <template #activator="{attrs}">
        <space-card-button
          v-bind="attrs"
          :extension="{
            icon: 'fa-ellipsis-v',
            title: $t('spacesList.button.options'),
            loading: sendingAction,
            click: () => menu = !menu,
          }"
          :space="space"
          icon />
      </template>
      <v-list max-width="300" dense>
        <v-list-item
          v-if="space.isMember"
          :aria-label="$t('spacesList.button.leave')"
          class="leaveSpace ps-0 py-0"
          link
          @click="leaveConfirm">
          <v-list-item-icon class="mx-2">
            <v-icon
              class="ma-auto"
              size="18">
              fa-sign-out-alt
            </v-icon>
          </v-list-item-icon>
          <v-list-item-content class="d-inline">
            <v-list-item-title class="text-body">{{ $t('spacesList.button.leave') }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <template v-else-if="space.isInvited">
          <v-list-item
            :aria-label="$t('spacesList.button.acceptToJoin')"
            class="acceptToJoin ps-0 py-0"
            link
            @click="acceptToJoin">
            <v-list-item-icon class="mx-2">
              <v-icon
                class="ma-auto"
                color="success"
                size="18">
                fa-check
              </v-icon>
            </v-list-item-icon>
            <v-list-item-content class="d-inline">
              <v-list-item-title class="text-body">{{ $t('spacesList.button.acceptToJoin') }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
          <v-list-item
            :aria-label="$t('spacesList.button.refuseToJoin')"
            class="refuseToJoin ps-0 py-0"
            link
            @click="refuseToJoin">
            <v-list-item-icon class="mx-2">
              <v-icon
                class="ma-auto"
                color="error"
                size="18">
                fa-times
              </v-icon>
            </v-list-item-icon>
            <v-list-item-content class="d-inline">
              <v-list-item-title class="text-body">{{ $t('spacesList.button.refuseToJoin') }}</v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </template>
        <v-list-item
          v-else-if="space.isPending"
          :aria-label="$t('spacesList.button.cancelRequest')"
          class="leaveSpace ps-0 py-0"
          link
          @click="cancelRequest">
          <v-list-item-icon class="mx-2">
            <v-icon
              class="ma-auto"
              color="error"
              size="18">
              fa-times
            </v-icon>
          </v-list-item-icon>
          <v-list-item-content class="d-inline">
            <v-list-item-title class="text-body">{{ $t('spacesList.button.cancelRequest') }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <v-list-item
          v-else-if="space.subscription === 'open' && !space.isMember"
          :aria-label="$t('spacesList.button.join')"
          class="joinSpace ps-0 py-0"
          link
          @click="join">
          <v-list-item-icon class="mx-2">
            <v-icon
              class="ma-auto"
              size="18">
              fa-sign-out-alt
            </v-icon>
          </v-list-item-icon>
          <v-list-item-content class="d-inline">
            <v-list-item-title class="text-body">{{ $t('spacesList.button.join') }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <v-list-item
          v-else-if="space.subscription === 'validation' && !space.isMember && !space.isPending"
          :aria-label="$t('spacesList.button.requestJoin')"
          class="requestJoinSpace ps-0 py-0"
          link
          @click="requestJoin">
          <v-list-item-icon class="mx-2">
            <v-icon
              class="ma-auto"
              size="18">
              fa-sign-out-alt
            </v-icon>
          </v-list-item-icon>
          <v-list-item-content class="d-inline">
            <v-list-item-title class="text-body">{{ $t('spacesList.button.requestJoin') }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <v-list-item
          :href="`/portal/s/${space.id}/settings`"
          :aria-label="$t('spacesList.button.edit')"
          class="editSpace ps-0 py-0"
          link>
          <v-list-item-icon class="mx-2">
            <v-icon
              class="ma-auto"
              size="18">
              far fa-edit
            </v-icon>
          </v-list-item-icon>
          <v-list-item-content class="d-inline">
            <v-list-item-title class="text-body">{{ $t('spacesList.button.edit') }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <v-list-item
          :aria-label="$t('spacesList.button.remove')"
          class="removeSpace ps-0 py-0"
          link
          @click="removeSpaceConfirm">
          <v-list-item-icon class="mx-2">
            <v-icon
              class="ma-auto"
              color="error"
              size="18">
              fa-trash
            </v-icon>
          </v-list-item-icon>
          <v-list-item-content class="d-inline">
            <v-list-item-title class="text-body error--text">{{ $t('spacesList.button.remove') }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </component>
    <space-card-button
      v-else-if="space.isMember"
      :extension="{
        icon: 'fa-sign-out-alt',
        title: $t('spacesList.button.leave'),
        loading: sendingAction,
        click: leaveConfirm
      }"
      :space="space"
      icon />
    <component
      v-else-if="space.isInvited"
      :is="$root.isMobile && 'v-bottom-sheet' || 'v-menu'"
      ref="actionMenu"
      v-model="menu"
      :attach="$root.isMobile && '#vuetify-apps' || attachMenu"
      :left="!$vuetify.rtl"
      :right="$vuetify.rtl"
      transition="slide-x-reverse-transition"
      content-class="position-absolute application-menu z-index-modal"
      offset-y>
      <template #activator="{attrs}">
        <space-card-button
          v-bind="attrs"
          :extension="{
            icon: 'fa-gift pink--text lighten-1',
            title: $t('spacesList.button.invited'),
            loading: sendingAction,
            click: () => menu = !menu,
          }"
          :space="space"
          class="mx-2" />
      </template>
      <v-list max-width="300" dense>
        <v-list-item
          :aria-label="$t('spacesList.button.acceptToJoin')"
          class="acceptToJoin ps-0 py-0"
          link
          @click="acceptToJoin">
          <v-list-item-icon class="mx-2">
            <v-icon
              class="ma-auto"
              color="success"
              size="18">
              fa-check
            </v-icon>
          </v-list-item-icon>
          <v-list-item-content class="d-inline">
            <v-list-item-title class="text-body">{{ $t('spacesList.button.acceptToJoin') }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <v-list-item
          :aria-label="$t('spacesList.button.refuseToJoin')"
          class="refuseToJoin ps-0 py-0"
          link
          @click="refuseToJoin">
          <v-list-item-icon class="mx-2">
            <v-icon
              class="ma-auto"
              color="error"
              size="18">
              fa-times
            </v-icon>
          </v-list-item-icon>
          <v-list-item-content class="d-inline">
            <v-list-item-title class="text-body">{{ $t('spacesList.button.refuseToJoin') }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </component>
    <space-card-button
      v-else-if="space.isPending"
      :extension="{
        title: $t('spacesList.button.cancelRequest'),
        loading: sendingAction,
        click: cancelRequest
      }"
      :space="space"
      class="mx-2" />
    <space-card-button
      v-else-if="space.subscription === 'open' && !space.isMember"
      :extension="{
        title: $t('spacesList.button.join'),
        loading: sendingAction,
        click: join
      }"
      :space="space"
      class="mx-2" />
    <space-card-button
      v-else-if="space.subscription === 'validation' && !space.isMember && !space.isPending"
      :extension="{
        title: $t('spacesList.button.requestJoin'),
        loading: sendingAction,
        click: requestJoin
      }"
      :space="space"
      class="mx-2" />
    <confirm-dialog
      v-if="confirmDialog"
      ref="confirmDialog"
      :title="confirmTitle"
      :message="confirmMessage"
      :ok-label="$t('spacesList.label.ok')"
      :cancel-label="okMethod && $t('spacesList.label.cancel')"
      @ok="okConfirmDialog"
      @dialog-closed="closeConfirmDialog" />
  </div>
</template>
<script>
export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    id: Math.random(), // NOSONAR
    sendingAction: false,
    confirmTitle: '',
    confirmMessage: '',
    confirmDialog: false,
    menu: false,
    okMethod: null,
  }),
  watch: {
    menu() {
      // Workaround to fix closing menu when clicking outside
      if (this.menu) {
        document.addEventListener('mousedown', this.closeMenu);
        this.$root.$emit('spaces-list-menu-opened', this.id);
      } else {
        document.removeEventListener('mousedown', this.closeMenu);
      }
    },
    spaceBannerUrl() {
      return this.space && (this.space.bannerUrl || `/portal/rest/v1/social/spaces/${this.space.prettyName}/banner`);
    },
  },
  created() {
    this.$root.$on('spaces-list-menu-opened', this.closeMenu);
  },
  beforeDestroy() {
    this.$root.$off('spaces-list-menu-opened', this.closeMenu);
    document.removeEventListener('mousedown', this.closeMenu);
  },
  methods: {
    removeSpaceConfirm() {
      this.openConfirmDialog(
        this.$t('spacesList.title.deleteSpace'),
        this.$t('spacesList.message.deleteSpace'),
        this.removeSpace);
    },
    removeSpace() {
      this.sendingAction = true;
      this.$spaceService.removeSpace(this.space.id)
        .then(() => this.$root.$emit('spaces-list-refresh'))
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
        .then(() => this.$root.$emit('spaces-list-refresh'))
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
        .then(() => this.$root.$emit('spaces-list-refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    refuseToJoin() {
      this.sendingAction = true;
      this.$spaceService.deny(this.space.id)
        .then(() => this.$root.$emit('spaces-list-refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    join() {
      this.sendingAction = true;
      this.$spaceService.join(this.space.id)
        .then(() => this.$root.$emit('spaces-list-refresh'))
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
        .then(() => this.$root.$emit('spaces-list-refresh'))
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
        .then(() => this.$root.$emit('spaces-list-refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    async openConfirmDialog(title, message, okMethod) {
      this.confirmDialog = true;
      await this.$nextTick();
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
    closeMenu(event) {
      if (event !== this.id) {
        if (event?.target) {
          window.setTimeout(() => {
            this.menu = false;
          }, 200);
        } else {
          this.menu = false;
        }
      }
    },
    closeConfirmDialog() {
      this.confirmTitle = '';
      this.confirmMessage = '';
      this.okMethod = null;
      this.confirmDialog = false;
    },
  },
};
</script>

