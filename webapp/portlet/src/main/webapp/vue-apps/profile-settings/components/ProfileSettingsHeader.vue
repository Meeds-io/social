/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

<template>
  <div id="settingsHeader" class="d-flex flex-row">
    <button
      class="btn btn-primary primary px-2 py-0"
      @click="openCreateDrawer()">
      <v-icon
        dark>
        mdi-plus
      </v-icon>
      {{ $t('profileSettings.button.addNew') }}
    </button>
    <v-spacer />
    <select
      id="filterSettingsSelect"
      v-model="filter"
      class="width-auto my-auto ignore-vuetify-classes d-none d-sm-inline"
      @change="changeSettingsFilter">
      <option
        v-for="item in filterSettings"
        :key="item.name"
        :value="item.name">
        {{ $t('profileSettings.filter.'+item.name.toLowerCase()) }}
      </option>
    </select>
  </div>
</template>

<script>
export default {
  props: {
    filter: {
      type: String,
      default: 'Active'
    },
  },
  data: () => ({
    filterSettings: [{name: 'Active'},{name: 'Inactive'},{name: 'All'}],
  }),

  methods: {
    openCreateDrawer() {
      this.$root.$emit('open-settings-create-drawer');
    },
    changeSettingsFilter(){
      this.$root.$emit('settings-set-filter', this.filter);
    },
  }
};
</script>