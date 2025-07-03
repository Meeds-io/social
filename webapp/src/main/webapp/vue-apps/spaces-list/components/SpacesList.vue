<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-app>
    <v-hover v-model="$root.hover">
      <v-main class="application-body">
        <spaces-toolbar
          :filter="filter"
          :filter-message="$t('spacesList.label.spacesSize', {0: spacesSize})"
          :filters-count="filtersCount"
          :can-create-space="canCreateSpace"
          compact-display
          @keyword-changed="keyword = $event"
          @loading="loadingSpaces = loadingSpaces || $event" />
        <categories-filter
          v-if="$root.allowFilteringPerCategory"
          v-show="spacesExists"
          v-model="$root.selectedCategoryId"
          :setting-name="$root.settingName"
          :category-depth="$root.categoryDepth"
          :filter-type="$root.filterType"
          :category-ids="$root.categoryIds"
          :exclude-category-ids="$root.excludeCategoryIds"
          object-type="space"
          class="full-width border-box-sizing px-5" />
        <spaces-card-list
          ref="spacesList"
          :keyword="keyword"
          :filter="filter"
          :loading-spaces="loadingSpaces"
          :spaces-size="spacesSize"
          class="px-3"
          @loading-spaces="loadingSpaces = $event"
          @loaded="spacesLoaded" />
      </v-main>
    </v-hover>
    <spaces-list-settings-drawer
      v-if="$root.canEdit" />
    <spaces-list-filter-drawer />
    <space-form-drawer />
    <spaces-pending-drawer />
  </v-app>    
</template>
<script>
export default {
  props: {
    canCreateSpace: {
      type: Boolean,
      default: false,
    },
    filter: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    keyword: null,
    spacesSize: 0,
    loadingSpaces: false,
    initialized: false,
    spacesExists: false,
  }),
  computed: {
    filtersCount() {
      return this.filter !== 'all' ? 1 : 0;
    },
    selectedCategoryId() {
      return this.$root.selectedCategoryId;
    },
  },
  watch: {
    selectedCategoryId() {
      this.loadingSpaces = true;
    },
    spacesSize() {
      if (!this.spacesExists && this.spacesSize) {
        this.spacesExists = true;
      }
    },
  },
  methods: {
    spacesLoaded(spacesSize) {
      this.spacesSize = spacesSize;
      if (!this.initialized) {
        this.$root.$applicationLoaded();
        this.initialized = true;
      }
    }
  },
};
</script>