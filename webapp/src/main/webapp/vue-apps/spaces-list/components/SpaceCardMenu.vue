<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or

 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <div class="d-flex align-center">
    <space-favorite-action
      v-if="space.isMember"
      :is-favorite="space.isFavorite"
      :space-id="space.id"
      :icon-size="20"
      class="ms-1" />
    <space-card-button
      v-for="(extension, i) in spaceActionExtensions"
      :key="i"
      :extension="extension"
      :space="space"
      class="ms-1" />
    <component
      v-if="space.canEdit || space.isMember"
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
      <v-list
        :max-width="!$root.isMobile && 300 || 'auto'"
        :class="$root.isMobile && 'border-top-left-radius border-top-right-radius'"
        dense>
        <space-card-menu-item
          v-if="$root.isMobile"
          :href="url"
          label="spacesList.button.openSpace"
          icon="fa-external-link-alt" />
        <space-card-menu-item
          label="spacesList.button.copyLink"
          icon="fa-link"
          @click="copyLink" />
        <space-card-menu-item
          v-if="spacePublicSiteUrl"
          :href="spacePublicSiteUrl"
          label="spacesList.button.visitPublicSite"
          icon="fa-globe" />
        <space-card-menu-item
          v-if="space.isMember && !space.isUserBound"
          label="spacesList.button.leave"
          icon="fa-sign-out-alt"
          @click="leaveConfirm" />
        <template v-else-if="space.isInvited">
          <space-card-menu-item
            label="spacesList.button.acceptToJoin"
            icon="fa-check"
            icon-color="success"
            @click="acceptToJoin" />
          <space-card-menu-item
            label="spacesList.button.refuseToJoin"
            icon="fa-times"
            icon-color="error"
            @click="refuseToJoin" />
        </template>
        <space-card-menu-item
          v-else-if="space.isPending"
          label="spacesList.button.cancelRequest"
          icon="fa-times"
          icon-color="error"
          @click="cancelRequest" />
        <space-card-menu-item
          v-else-if="space.subscription === 'open' && !space.isMember"
          label="spacesList.button.join"
          icon="fa-sign-in-alt"
          @click="join" />
        <space-card-menu-item
          v-else-if="space.subscription === 'validation' && !space.isMember && !space.isPending"
          label="spacesList.button.requestJoin"
          icon="fa-sign-in-alt"
          @click="requestJoin" />
        <space-card-menu-item
          v-if="space.canEdit"
          :href="`${url}/settings`"
          label="spacesList.button.openSettings"
          icon="fa-edit" />
        <space-card-menu-item
          v-if="space.canEdit"
          label="spacesList.button.remove"
          label-color="error"
          icon="fa-trash"
          icon-color="error"
          @click="removeSpaceConfirm" />
      </v-list>
    </component>
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
            icon: 'fa fa-question warning--text',
            title: $t('spacesList.button.answerInvitation'),
            loading: sendingAction,
            click: () => menu = !menu,
          }"
          :space="space"
          class="mx-2" />
      </template>
      <v-list
        :max-width="!$root.isMobile && 300 || 'auto'"
        :class="$root.isMobile && 'border-top-left-radius border-top-right-radius'"
        dense>
        <space-card-menu-item
          label="spacesList.button.acceptToJoin"
          icon="fa-check"
          icon-color="success"
          @click="acceptToJoin" />
        <space-card-menu-item
          label="spacesList.button.refuseToJoin"
          icon="fa-times"
          icon-color="error"
          @click="refuseToJoin" />
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
    spaceActionExtensions: {
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
  computed: {
    url() {
      return `${eXo.env.portal.context}/s/${this.space.id}`;
    },
    spacePublicSiteUrl() {
      return this.space.publicSiteName
        && this.space.publicSiteVisibility !== 'manager'
        && `${eXo.env.portal.context}/${this.space.publicSiteName}`;
    },
  },
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
    copyLink() {
      try {
        navigator.clipboard.writeText(`${window.location.origin}${eXo.env.portal.context}/s/${this.space.id}`);
        this.$root.$emit('alert-message', this.$t('SpaceSettings.publicSite.drawer.copyLink.success'), 'success');
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('SpaceSettings.publicSite.drawer.copyLink.error'), 'warning');
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

