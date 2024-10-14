<!--

 This file is part of the Meeds project (https://meeds.io/).
 
 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 
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
    :message="$t(confirmMessageKey)"
    :title="$t(confirmTitleKey)"
    :ok-label="$t(confirmOkKey)"
    :cancel-label="$t(confirmCancelKey)"
    @ok="clickCallback && clickCallback()"
    @dialog-opened="dialogOpened = true"
    @dialog-closed="dialogOpened = false" />
</template>

<script>
export default {
  data: () => ({
    confirmMessageKey: null,
    confirmTitleKey: null,
    confirmOkKey: null,
    confirmCancelKey: null,
    clickCallback: null,
    dialogOpened: false,
  }),
  watch: {
    dialogOpened() {
      if (this.dialogOpened) {
        this.$root.$emit('spaces-administration-retore-home-layout-confirm-opened');
      } else {
        this.$root.$emit('spaces-administration-retore-home-layout-confirm-closed');
      }
    },
  },
  created() {
    this.$root.$on('spaces-administration-retore-home-layout-confirm', action => {
      this.confirmTitleKey = action.title;
      this.confirmMessageKey = action.message;
      this.confirmOkKey = action.ok;
      this.confirmCancelKey = action.cancel;
      this.clickCallback = action.callback;
      this.$refs.confirmDialog.open();
    });
  },
};
</script>