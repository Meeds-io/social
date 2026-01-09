<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
    :cancel-label="$t('menu.confirmation.cancel')"
    @ok="confirmLeaveSpace"
    @opened="$root.$emit('dialog-opened')"
    @closed="$root.$emit('dialog-closed')" />
</template>
<script>
export default {
  data: () => ({
    selectedSpace: null,
  }),
  computed: {
    title() {
      return this.isOnlyManagerLeftInSpace && this.$t('menu.confirmation.title.leaveSpace.warning')
          || this.$t('menu.confirmation.title.leaveSpace');
    },
    confirmMessage() {
      return this.isOnlyManagerLeftInSpace && this.$t('menu.confirmation.message.leaveSpace.warning') || this.$t('menu.confirmation.message.leaveSpace', {
        0: `<b>${this.spaceDisplayName}</b>`,
      });
    },
    spaceDisplayName() {
      return this.selectedSpace?.displayName;
    },
    isOnlyManagerLeftInSpace() {
      return this.selectedSpace?.isManager && this.selectedSpace?.managersCount <= 1;
    }
  },
  created() {
    this.$root.$on('leave-space', this.leaveSpace);
  },
  beforeDestroy() {
    this.$root.$off('leave-space', this.leaveSpace);
  },
  methods: {
    leaveSpace(space) {
      this.selectedSpace = space;
      this.openDialog();
    },
    async openDialog() {
      await this.$nextTick();
      this.$refs?.confirmDialog?.open?.();
    },
    confirmLeaveSpace() {
      if (!this.isOnlyManagerLeftInSpace) {
        this.$spaceService.leave(this.selectedSpace.id);
      }
    }
  },
};
</script>