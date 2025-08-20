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
  <div class="spaceFilter d-flex flex-row">
    <v-menu
      offset-y
      left
      :close-on-content-click="true">
      <template #activator="{ on, attrs }">
        <v-chip
          outlined
          tab-index="0"
          class="text-body text-header-color mx-1"
          v-bind="attrs"
          v-on="on">
          <v-icon size="22" class="pe-2">fas fa-sort</v-icon>
          <span class="me-2">{{ selectedSortLabel }}</span>
          <i class="fas fa-chevron-down"></i>
        </v-chip>
      </template>

      <v-list>
        <v-list-item
          v-for="option in sortTypes"
          :key="option.value"
          @click="selectSort(option)">
          <v-list-item-title>{{ option.label }}</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>

<script>
export default {
  data() {
    return {
      selectedSort: '',
      sortDescending: true,
    };
  },
  computed: {
    sortTypes() {
      return [
        { value: '', label: this.$t('search.sort.by.relevancy.option') },
        { value: 'date', label: this.$t('search.sort.by.date.option') },
      ];
    },
    selectedSortLabel() {
      return this.$t(`search.sort.by.${this.selectedSort || 'relevancy'}`);
    }
  },
  methods: {
    selectSort(option) {
      this.selectedSort = option.value;
      this.$root.$emit('sort-changed', option, this.sortDescending);
      if (this.selectedSort === 'date') {
        document.dispatchEvent(new CustomEvent('search-sort-by-date-action'));
      }
    },
  },
};
</script>