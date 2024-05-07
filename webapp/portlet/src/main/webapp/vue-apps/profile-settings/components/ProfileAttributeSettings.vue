<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2024 Meeds Association contact@meeds.io
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
-->

<template>
  <div>
    <div class="ms-n2 mb-5 d-inline-flex">
      <v-btn
        :title="$t('profileSettings.label.profile')"
        size="24"
        icon
        @click="close">
        <v-icon
          size="19"
          class="icon-default-color">
          {{ $vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left' }}
        </v-icon>
      </v-btn>
      <h4 class="font-weight-bold">
        {{ $t('profileSettings.label.profile') }}
      </h4>
    </div>
    <profile-settings-header :filter="filter" />
    <profile-settings-table :settings="filteredSettings" />
    <profile-setting-form-drawer
      :settings="settings"
      :languages="languagesData"
      :un-hiddenable-properties="unHiddenableProperties" />
  </div>
</template>

<script>
export default {
  props: {
    languages: {
      type: Array,
      default: null,
    },
    settings: {
      type: Array,
      default: null,
    },
    unHiddenableProperties: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    filter: 'Active',
    languagesData: null
  }),
  created() {
    this.$root.$on('settings-set-filter', this.setFilter);
    this.languagesData = [...this.languages].sort((a, b) => a.value.localeCompare(b.value));
    this.$root.$on('settings-set-filter', this.setFilter);
  },
  computed: {
    filteredSettings(){
      if (this.filter === 'Active') {
        return this.settings.filter(function (setting) {
          return setting.active;
        });
      } else if (this.filter === 'Inactive') {
        return this.settings.filter(function (setting) {
          return !setting.active;
        });
      } return this.settings;
    }
  },
  methods: {
    setFilter(filter) {
      this.filter = filter;
    },
    close() {
      this.$emit('back-to-main-page');
    },
    setFilter(filter) {
      this.filter = filter;
    }
  }
};
</script>
