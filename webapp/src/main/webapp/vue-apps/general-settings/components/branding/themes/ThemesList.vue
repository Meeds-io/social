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
  <div class="themesList mb-6">
    <div class="d-flex align-center">
      <div class="text-header">
        {{ $t('generalSettings.themes.title') }}
      </div>
      <v-spacer />
      <v-tooltip bottom>
        <template #activator="{ on, attrs }">
          <v-btn
            v-bind="attrs"
            v-on="on"
            :aria-label="$t('generalSettings.themes.add.tooltip')"
            class="themesAddButton"
            icon
            @click="$emit('add')">
            <v-icon size="18" class="icon-default-color">fa-plus</v-icon>
          </v-btn>
        </template>
        {{ $t('generalSettings.themes.add.tooltip') }}
      </v-tooltip>
    </div>
    <v-radio-group
      :value="themeInUseKey"
      class="mt-2"
      hide-details
      @change="$emit('select', $event)">
      <portal-general-settings-themes-list-item
        v-for="theme in themes"
        :key="theme.key"
        :theme="theme"
        @edit="$emit('edit', theme)"
        @restore="askRestore(theme)"
        @delete="askDelete(theme)" />
    </v-radio-group>
    <confirm-dialog
      ref="deleteConfirmDialog"
      :title="$t('generalSettings.theme.delete.title')"
      :message="$t('generalSettings.theme.delete.message', {0: deleteTheme && deleteTheme.name})"
      :ok-label="$t('generalSettings.theme.confirm')"
      :cancel-label="$t('generalSettings.button.cancel')"
      @ok="$emit('delete', deleteTheme)" />
    <confirm-dialog
      ref="restoreConfirmDialog"
      :title="$t('generalSettings.theme.restore.title')"
      :message="$t('generalSettings.theme.restore.message', {0: restoreTheme && restoreTheme.name})"
      :ok-label="$t('generalSettings.theme.confirm')"
      :cancel-label="$t('generalSettings.button.cancel')"
      @ok="$emit('restore', restoreTheme)" />
  </div>
</template>
<script>
export default {
  props: {
    themes: {
      type: Array,
      default: () => [],
    },
    themeInUseKey: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    deleteTheme: null,
    restoreTheme: null,
  }),
  methods: {
    askDelete(theme) {
      this.deleteTheme = theme;
      this.$refs.deleteConfirmDialog.open();
    },
    askRestore(theme) {
      this.restoreTheme = theme;
      this.$refs.restoreConfirmDialog.open();
    },
  },
};
</script>
