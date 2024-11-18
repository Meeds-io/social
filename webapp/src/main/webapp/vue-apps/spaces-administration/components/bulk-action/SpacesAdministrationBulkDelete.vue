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
  <div class="d-inline">
    <v-btn
      :disabled="$root.isBulkProcessing"
      color="error"
      elevation="0"
      outlined
      @click="openConfirmDialog">
      <v-icon size="16" class="me-2">fa-trash</v-icon>
      {{ $t('social.spaces.administration.manageSpaces.delete') }}
    </v-btn>
    <confirm-dialog
      ref="dialog"
      :title="$t('social.spaces.administration.manageSpaces.deleteConfirmTitle')"
      :message="$t('social.spaces.administration.manageSpaces.deleteSpacesConfirmMessage', {0: `<strong>${spacesCount}</strong>`})"
      :ok-label="$t('social.spaces.administration.manageSpaces.confirm')"
      :cancel-label="$t('social.spaces.administration.manageSpaces.cancel')"
      @ok="deleteSpaces"
      @closed="close" />
  </div>
</template>
<script>
export default {
  data: () => ({
    successMessage: null,
  }),
  computed: {
    spacesCount() {
      return this.$root.allSpacesSelected ? this.$root.spacesSize : this.$root.selectedSpaces.length;
    },
  },
  methods: {
    openConfirmDialog() {
      window.setTimeout(() => this.$refs.dialog.open(), 200);
    },
    deleteSpaces() {
      // Workaround for context change, compute success message on processing start
      this.successMessage = this.$t('social.spaces.administration.manageSpaces.spacesDeletedSuccessfully', {
        0: this.spacesCount
      });
      this.$root.applyOperationInBulk(
        space => this.$spaceService.removeSpace(space.id),
        null,
        () => {
          this.$root.$emit('alert-message', this.successMessage, 'success');
          this.$root.$emit('spaces-administration-list-refresh');
        });
    },
    close() {
      window.setTimeout(() => this.dialog = false, 200);
    },
  },
};
</script>