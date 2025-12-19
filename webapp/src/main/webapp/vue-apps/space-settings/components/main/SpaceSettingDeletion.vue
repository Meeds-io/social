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
  <div v-if="$root.isAllSections">
    <v-card flat>
      <v-list class="pa-0" dense>
        <v-list-item class="pa-0">
          <v-list-item-content>
            <v-list-item-title class="text-title error--text py-1">
              {{ $t('spaceSetting.delete.label') }}
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              small
              icon
              @click="removeSpaceConfirm">
              <v-icon size="18" class="error--text">fa-trash</v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-list>
    </v-card>
    <exo-confirm-dialog
      ref="deleteSpaceConfirmDialog"
      :message="$t('spaceSetting.message.deleteSpace')"
      :title="$t('spaceSetting.delete.label')"
      :ok-label="$t('spaceSetting.delete.confirm.label')"
      :cancel-label="$t('spaceSetting.delete.cancel.label')"
      @ok="removeSpace" />
  </div>
</template>
<script>
export default {
  computed: {
    space() {
      return this.$root.space;
    }
  },
  methods: {
    removeSpaceConfirm() {
      this.$refs.deleteSpaceConfirmDialog.open();
    },
    async removeSpace() {
      try {
        await this.$spaceService.removeSpace(this.space?.id);
        this.$root.$emit('alert-message', this.$t('spaceSetting.spaceDeletedSuccessfully'), 'success');
        window.location.href = eXo?.env?.portal?.context;
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('spaceSetting.spaceDeletionError', {0: this.space?.displayName}), 'error');
      }
    }
  }
};
</script>