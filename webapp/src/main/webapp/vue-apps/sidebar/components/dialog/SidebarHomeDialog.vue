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
    :title="$t('menu.confirmation.title.changeHome')"
    :message="confirmMessage"
    :ok-label="$t('menu.confirmation.ok')"
    :cancel-label="$t('menu.confirmation.cancel')"
    @ok="changeHome"
    @opened="$root.$emit('dialog-opened')"
    @closed="$root.$emit('dialog-closed')" />
</template>
<script>
export default {
  data: () => ({
    selectedSpace: null,
    selectedPage: null,
  }),
  computed: {
    name() {
      return this.selectedSpace?.displayName || this.selectedPage?.name;
    },
    url() {
      return this.selectedPage?.url || `${eXo.env.portal.context}/s/${this.selectedSpace?.id}`;
    },
    confirmMessage() {
      return this.$t('menu.confirmation.message.changeHome', {
        0: `<b>${this.name}</b>`,
      });
    },
  },
  created() {
    this.$root.$on('change-home-link-space', this.selectSpaceHome);
    this.$root.$on('update-home-link-page', this.selectPageHome);
  },
  beforeDestroy() {
    this.$root.$off('change-home-link-space', this.selectSpaceHome);
    this.$root.$off('update-home-link-page', this.selectPageHome);
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
      this.selectedSpace = space;
      this.selectedPage = null;
      this.openDialog();
    },
    selectPageHome(page) {
      this.selectedPage = page;
      this.selectedSpace = null;
      this.openDialog();
    },
    async openDialog() {
      await this.$nextTick();
      if (this.$root.defaultUserPath === this.url) {
        return;
      }
      this.$refs?.confirmDialog?.open?.();
    },
  },
};
</script>