<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <v-list-item
    dense>
    <v-list-item-content>
      <space-avatar
        :space="space"
        class="text-truncate"
        list-style />
    </v-list-item-content>
    <v-list-item-action class="my-auto me-0 ms-n2">
      <v-btn
        small
        icon
        @click="openConfirmDialog">
        <v-icon size="18" color="error">
          fa-trash
        </v-icon>
      </v-btn>
    </v-list-item-action>
    <confirm-dialog
      v-if="dialog"
      ref="dialog"
      :title="$t('spaceSetting.subspaces.deleteConfirmTitle')"
      :message="$t('spaceSetting.subspaces.deleteConfirmMessage', {0: `<br><strong>${displayName}</strong>`})"
      :ok-label="$t('spaceSetting.subspaces.confirm')"
      :cancel-label="$t('spaceSetting.subspaces.cancel')"
      @ok="deleteSpace"
      @closed="close" />
  </v-list-item>
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
    dialog: false,
  }),
  computed: {
    displayName() {
      return this.space?.displayName;
    },
  },
  methods: {
    async openConfirmDialog() {
      this.dialog = true;
      await this.$nextTick();
      window.setTimeout(() => this.$refs.dialog.open(), 200);
    },
    close() {
      window.setTimeout(() => this.dialog = false, 200);
    },
    async deleteSpace() {
      this.$emit('loading', true);
      try {
        await this.$spaceService.removeSpace(this.space.id);
        this.$root.$emit('subspaces-list-refresh');
        this.$root.$emit('alert-message', this.$t('spaceSetting.subspaces.subspaceDeletedSuccessfully'), 'success');
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('spaceSetting.subspaces.subspaceDeletionError', {0: this.displayName}), 'error');
      } finally {
        this.$emit('loading', false);
      }
    },
  },
};
</script>