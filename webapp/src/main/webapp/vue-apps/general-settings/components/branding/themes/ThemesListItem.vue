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
  <div class="themesListItem d-flex align-center">
    <v-radio
      :value="theme.key"
      :label="theme.name"
      :class="`themeRadio ${isDefault && 'defaultTheme' || 'customTheme'}`"
      class="flex-grow-1 my-1" />
    <v-tooltip bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          v-bind="attrs"
          v-on="on"
          :aria-label="$t('generalSettings.edit.button.tooltip')"
          class="themeEditButton"
          icon
          @click="$emit('edit')">
          <v-icon size="18" class="icon-default-color">fa-edit</v-icon>
        </v-btn>
      </template>
      {{ $t('generalSettings.edit.button.tooltip') }}
    </v-tooltip>
    <v-tooltip v-if="deletable" bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          v-bind="attrs"
          v-on="on"
          :aria-label="$t('generalSettings.theme.delete.tooltip')"
          class="themeDeleteButton"
          icon
          @click="$emit('delete')">
          <v-icon size="18" color="error">fa-trash</v-icon>
        </v-btn>
      </template>
      {{ $t('generalSettings.theme.delete.tooltip') }}
    </v-tooltip>
    <v-tooltip v-else bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          v-bind="attrs"
          v-on="on"
          :aria-label="$t('generalSettings.restore.button.tooltip')"
          class="themeRestoreButton"
          icon
          @click="$emit('restore')">
          <v-icon size="18" class="icon-default-color">fa-undo</v-icon>
        </v-btn>
      </template>
      {{ $t('generalSettings.restore.button.tooltip') }}
    </v-tooltip>
  </div>
</template>
<script>
export default {
  props: {
    theme: {
      type: Object,
      required: true,
    },
  },
  computed: {
    isDefault() {
      return this.theme.origin === 'DEFAULT';
    },
    // only a theme created in the page can be deleted: the Default Theme and a configured one are restored
    deletable() {
      return this.theme.origin === 'UI';
    },
  },
};
</script>
