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
    <space-card-button
      v-if="spacePublicSiteUrl"
      class="mx-2"
      :extension="{
        icon: 'fas fa-globe',
        iconOnly: true,
        title: $t('spacesList.button.visitPublicSite'),
        click: openSpacePublicSiteUrl
      }"
      :space="space" />
    <space-favorite-action
      v-if="space.isMember"
      class="ms-1"
      :icon-size="20"
      :is-favorite="space.isFavorite"
      :space-id="space.id" />
    <space-card-button
      v-for="(extension, i) in spaceActionExtensions"
      :key="i"
      class="ms-1"
      :extension="extension"
      :space="space" />
    <component
      :is="$root.isMobile && 'v-bottom-sheet' || 'v-menu'"
      v-if="space.canEdit || space.isMember"
      ref="actionMenu"
      v-model="menu"
      :attach="$root.isMobile && '#vuetify-apps' || attachMenu"
      content-class="position-absolute application-menu z-index-modal"
      :left="!$vuetify.rtl"
      offset-y
      :right="$vuetify.rtl"
      transition="slide-x-reverse-transition">
      <template #activator="{attrs}">
        <space-card-button
          v-bind="attrs"
          :extension="{
            icon: 'fa-ellipsis-v',
            title: $t('spacesList.button.options'),
            loading: sendingAction,
            click: () => menu = !menu,
          }"
          icon
          :space="space" />
      </template>
      <v-list
        :class="$root.isMobile && 'border-top-left-radius border-top-right-radius'"
        dense
        :max-width="!$root.isMobile && 300 || 'auto'">
        <space-card-menu-item
          v-if="$root.isMobile"
          :href="url"
          icon="fa-external-link-alt"
          label="spacesList.button.openSpace" />
        <space-card-menu-item
          icon="fa-link"
          label="spacesList.button.copyLink"
          @click="copyLink" />
        <space-card-menu-item
          v-if="space.isMember && !space.isUserBound"
          icon="fa-sign-out-alt"
          label="spacesList.button.leave"
          @click="leaveConfirm" />
        <template v-else-if="space.isInvited">
          <space-card-menu-item
            icon="fa-check"
            icon-color="success"
            label="spacesList.button.acceptToJoin"
            @click="acceptToJoin" />
          <space-card-menu-item
            icon="fa-times"
            icon-color="error"
            label="spacesList.button.refuseToJoin"
            @click="refuseToJoin" />
        </template>
        <space-card-menu-item
          v-else-if="space.isPending"
          icon="fa-times"
          icon-color="error"
          label="spacesList.button.cancelRequest"
          @click="cancelRequest" />
        <space-card-menu-item
          v-else-if="space.subscription === 'open' && !space.isMember"
          icon="fa-sign-in-alt"
          label="spacesList.button.join"
          @click="join" />
        <space-card-menu-item
          v-else-if="space.subscription === 'validation' && !space.isMember && !space.isPending"
          icon="fa-sign-in-alt"
          label="spacesList.button.requestJoin"
          @click="requestJoin" />
        <space-card-menu-item
          v-if="space.canDelete"
          icon="fa-trash"
          icon-color="error"
          label="spacesList.button.remove"
          label-color="error"
          @click="removeSpaceConfirm" />
      </v-list>
    </component>
    <component
      :is="$root.isMobile && 'v-bottom-sheet' || 'v-menu'"
      v-else-if="space.isInvited"
      ref="actionMenu"
      v-model="menu"
      :attach="$root.isMobile && '#vuetify-apps' || attachMenu"
      content-class="position-absolute application-menu z-index-modal"
      :left="!$vuetify.rtl"
      offset-y
      :right="$vuetify.rtl"
      transition="slide-x-reverse-transition">
      <template #activator="{attrs}">
        <space-card-button
          v-bind="attrs"
          class="mx-2"
          :extension="{
            icon: 'fa fa-question warning--text',
            title: $t('spacesList.button.answerInvitation'),
            loading: sendingAction,
            click: () => menu = !menu,
          }"
          :space="space" />
      </template>
      <v-list
        :class="$root.isMobile && 'border-top-left-radius border-top-right-radius'"
        dense
        :max-width="!$root.isMobile && 300 || 'auto'">
        <space-card-menu-item
          icon="fa-check"
          icon-color="success"
          label="spacesList.button.acceptToJoin"
          @click="acceptToJoin" />
        <space-card-menu-item
          icon="fa-times"
          icon-color="error"
          label="spacesList.button.refuseToJoin"
          @click="refuseToJoin" />
      </v-list>
    </component>
    <space-card-button
      v-else-if="space.isPending"
      class="mx-2"
      :extension="{
        title: $t('spacesList.button.cancelRequest'),
        loading: sendingAction,
        click: cancelRequest
      }"
      :space="space" />
    <space-card-button
      v-else-if="space.subscription === 'open' && !space.isMember"
      class="mx-2"
      :extension="{
        title: $t('spacesList.button.join'),
        loading: sendingAction,
        click: join
      }"
      :space="space" />
    <space-card-button
      v-else-if="space.subscription === 'validation' && !space.isMember && !space.isPending"
      class="mx-2"
      :extension="{
        title: $t('spacesList.button.requestJoin'),
        loading: sendingAction,
        click: requestJoin
      }"
      :space="space" />
    <confirm-dialog
      v-if="confirmDialog"
      ref="confirmDialog"
      :cancel-label="okMethod && $t('spacesList.label.cancel')"
      :message="confirmMessage"
      :ok-label="$t('spacesList.label.ok')"
      :title="confirmTitle"
      @dialog-closed="closeConfirmDialog"
      @ok="okConfirmDialog" />
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
      url () {
        return `${eXo.env.portal.context}/s/${this.space.id}`;
      },
      publicSiteName () {
        return this.space?.publicSiteName;
      },
      spacePublicSiteUrl () {
        return this.publicSiteName && `${eXo.env.portal.context}/${this.publicSiteName}`;
      },
    },
    watch: {
      menu () {
        // Workaround to fix closing menu when clicking outside
        if (this.menu) {
          document.addEventListener('mousedown', this.closeMenu);
          this.$root.$emit('spaces-list-menu-opened', this.id);
        } else {
          document.removeEventListener('mousedown', this.closeMenu);
        }
      },
      spaceBannerUrl () {
        return this.space && (this.space.bannerUrl || `/portal/rest/v1/social/spaces/${this.space.prettyName}/banner`);
      },
    },
    created () {
      this.$root.$on('spaces-list-menu-opened', this.closeMenu);
    },
    beforeUnmount () {
      this.$root.$off('spaces-list-menu-opened', this.closeMenu);
      document.removeEventListener('mousedown', this.closeMenu);
    },
    methods: {
      removeSpaceConfirm () {
        this.openConfirmDialog(
          this.$t('spacesList.title.deleteSpace'),
          this.$t('spacesList.message.deleteSpace'),
          this.removeSpace);
      },
      removeSpace () {
        this.sendingAction = true;
        eXo.$spaceService.removeSpace(this.space.id)
          .then(() => this.$root.$emit('spaces-list-refresh'))
          .catch(e => {
           
            console.error('Error processing action', e);
          })
          .finally(() => {
            this.sendingAction = false;
          });
      },
      leaveConfirm () {
        const isOnlyManagrLeftInSpace = this.space.isManager && this.space.managersCount <= 1;
        if (isOnlyManagrLeftInSpace) {
          this.openConfirmDialog(
            this.$t('spacesList.warning'),
            this.$t('spacesList.warning.lastManager'));
        } else {
          this.openConfirmDialog(
            this.$t('spacesList.title.leaveSpace'),
            this.$t('spacesList.message.leaveSpace', { 0: `<b>${this.space.displayName}</b>` }),
            this.leave);
        }
      },
      leave () {
        this.sendingAction = true;
        eXo.$spaceService.leave(this.space.id)
          .then(() => this.$root.$emit('spaces-list-refresh'))
          .catch(e => {
           
            console.error('Error processing action', e);
          })
          .finally(() => {
            this.sendingAction = false;
          });
      },
      acceptToJoin () {
        this.sendingAction = true;
        eXo.$spaceService.accept(this.space.id)
          .then(() => this.$root.$emit('spaces-list-refresh'))
          .catch(e => {
           
            console.error('Error processing action', e);
          })
          .finally(() => {
            this.sendingAction = false;
          });
      },
      refuseToJoin () {
        this.sendingAction = true;
        eXo.$spaceService.deny(this.space.id)
          .then(() => this.$root.$emit('spaces-list-refresh'))
          .catch(e => {
           
            console.error('Error processing action', e);
          })
          .finally(() => {
            this.sendingAction = false;
          });
      },
      join () {
        if (this.$root.anonymous) {
          window.location.href = `${this.spacePublicSiteUrl ? this.spacePublicSiteUrl : '/portal/login'}`;
          return;
        }
        this.sendingAction = true;
        eXo.$spaceService.join(this.space.id)
          .then(() => this.$root.$emit('spaces-list-refresh'))
          .catch(e => {
           
            console.error('Error processing action', e);
          })
          .finally(() => {
            this.sendingAction = false;
          });
      },
      requestJoin () {
        if (this.$root.anonymous) {
          window.location.href = `${this.spacePublicSiteUrl ? this.spacePublicSiteUrl : '/portal/login'}`;
          return;
        }
        this.sendingAction = true;
        eXo.$spaceService.requestJoin(this.space.id)
          .then(() => this.$root.$emit('spaces-list-refresh'))
          .catch(e => {
           
            console.error('Error processing action', e);
          })
          .finally(() => {
            this.sendingAction = false;
          });
      },
      cancelRequest () {
        this.sendingAction = true;
        eXo.$spaceService.cancel(this.space.id)
          .then(() => this.$root.$emit('spaces-list-refresh'))
          .catch(e => {
           
            console.error('Error processing action', e);
          })
          .finally(() => {
            this.sendingAction = false;
          });
      },
      async openConfirmDialog (title, message, okMethod) {
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
      okConfirmDialog () {
        if (this.okMethod) {
          this.okMethod();
        }
      },
      closeMenu (event) {
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
      copyLink () {
        try {
          navigator.clipboard.writeText(`${window.location.origin}${eXo.env.portal.context}/s/${this.space.id}`);
          this.$root.$emit('alert-message', this.$t('SpaceSettings.publicSite.drawer.copyLink.success'), 'success');
        } catch (e) {
          this.$root.$emit('alert-message', this.$t('SpaceSettings.publicSite.drawer.copyLink.error'), 'warning');
        }
      },
      openSpacePublicSiteUrl () {
        window.location.href = `${this.spacePublicSiteUrl}`;
      },
      closeConfirmDialog () {
        this.confirmTitle = '';
        this.confirmMessage = '';
        this.okMethod = null;
        this.confirmDialog = false;
      },
    },
  };
</script>

