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
  <exo-confirm-dialog
    ref="confirmDialog"
    :title="title"
    :message="confirmMessage"
    :ok-label="$t('menu.confirmation.ok')"
    :cancel-label="actionClick && $t('menu.confirmation.cancel')"
    @ok="onConfirm"
    @opened="$root.$emit('dialog-opened')"
    @closed="onclose" />
</template>
<script>
export default {
  data: () => ({
    selectedSpace: null,
    selectedPage: null,
    actionName: null,
    actionClick: null,
  }),
  computed: {
    name() {
      return this.selectedSpace?.displayName || this.selectedPage?.name;
    },
    url() {
      return this.selectedPage?.url || `${eXo.env.portal.context}/s/${this.selectedSpace?.id}`;
    },
    confirmMessage() {
      return this.$t(`menu.confirmation.message.${this.actionName}`, {
        0: `<b>${this.name}</b>`,
      });
    },
    title() {
      return this.$t(`menu.confirmation.title.${this.actionName}`);
    }
  },
  created() {
    this.$root.$on('change-home-link-space', this.selectSpaceHome);
    this.$root.$on('update-home-link-page', this.selectPageHome);
    this.$root.$on('leave-space', this.leaveSpace);
  },
  beforeDestroy() {
    this.$root.$off('change-home-link-space', this.selectSpaceHome);
    this.$root.$off('update-home-link-page', this.selectPageHome);
    this.$root.$on('leave-space', this.leaveSpace);
  },
  methods: {
    changeHome() {
      this.$settingService.setSettingValue('USER', eXo.env.portal.userName, 'PORTAL', 'HOME', 'HOME_PAGE_URI', this.url)
        .then(() => {
          eXo.env.portal.homeLink = this.url;
          document.dispatchEvent(new CustomEvent('homeLinkUpdated', {detail: this.url}));
        });
    },
    selectSpaceHome(space) {
      this.actionName = 'changeHome';
      this.actionClick = () => this.changeHome();
      this.selectedSpace = space;
      this.selectedPage = null;
      if (this.$root.defaultUserPath === this.url) {
        return;
      }
      this.openDialog();
    },
    selectPageHome(page) {
      this.actionName = 'changeHome';
      this.actionClick = () => this.changeHome();
      this.selectedPage = page;
      this.selectedSpace = null;
      if (this.$root.defaultUserPath === this.url) {
        return;
      }
      this.openDialog();
    },
    async openDialog() {
      await this.$nextTick();
      this.$refs?.confirmDialog?.open?.();
    },
    leaveSpace(space) {
      this.selectedSpace = space;
      const isOnlyManagerLeftInSpace = this.selectedSpace.isManager && this.selectedSpace.managersCount <= 1;
      if (isOnlyManagerLeftInSpace) {
        this.actionName = 'leaveSpace.warning';
        this.actionClick = null;
        this.openDialog();
        return;
      }
      this.actionName = 'leaveSpace';
      this.actionClick = () => this.confirmLeaveSpace();
      this.openDialog();
    },
    confirmLeaveSpace() {
      this.$spaceService.leave(this.selectedSpace.id);
    },
    onConfirm() {
      if (typeof this.actionClick === 'function') {
        this.actionClick();
      }
    },
    onclose() {
      this.actionName = null;
      this.actionClick = null;
      this.$root.$emit('dialog-closed');
    },
  }
};
</script>