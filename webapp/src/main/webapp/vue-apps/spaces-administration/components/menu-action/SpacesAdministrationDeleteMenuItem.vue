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
  <v-list-item
    dense
    @click="openConfirmDialog">
    <v-icon class="error--text" size="13">fa-trash</v-icon>
    <v-list-item-title class="ps-2">
      <span class="error--text">{{ $t('social.spaces.administration.manageSpaces.delete') }}</span>
    </v-list-item-title>
    <confirm-dialog
      v-if="dialog"
      ref="dialog"
      :title="$t('social.spaces.administration.manageSpaces.deleteConfirmTitle')"
      :message="$t('social.spaces.administration.manageSpaces.deleteConfirmMessage', {0: `<br><strong>${space.displayName}</strong>`})"
      :ok-label="$t('social.spaces.administration.manageSpaces.confirm')"
      :cancel-label="$t('social.spaces.administration.manageSpaces.cancel')"
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
        this.$root.$emit('spaces-administration-list-refresh');
        this.$root.$emit('alert-message', this.$t('social.spaces.administration.manageSpaces.spaceDeletedSuccessfully'), 'success');
      } catch (e) {
        console.error(e);
        this.$root.$emit('alert-message', this.$t('social.spaces.administration.manageSpaces.spaceDeletionError', {0: this.space.displayName}), 'error');
      } finally {
        this.$emit('loading', false);
      }
    },
  },
};
</script>