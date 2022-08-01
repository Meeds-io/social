<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
  contact@meeds.io
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
  <v-flex class="d-flex content-box-sizing justify-end">
    <select
      v-model="filter"
      class="width-auto my-auto ignore-vuetify-classes d-none d-sm-inline"
      @change="applyFilter">
      <option
        v-for="streamFilter in streamFilters"
        :key="streamFilter.value"
        :value="streamFilter.value">
        {{ streamFilter.text }}
      </option>
    </select>
    <v-icon
      class="d-sm-none"
      @click="openBottomMenuFilter">
      fa-filter
    </v-icon>
    <v-bottom-sheet v-model="bottomMenu" class="pa-0">
      <v-sheet class="text-center">
        <v-toolbar
          color="primary"
          dark
          class="border-box-sizing">
          <v-btn text @click="bottomMenu = false">
            {{ $t('activity.filter.button.cancel') }}
          </v-btn>
          <v-spacer />
          <v-toolbar-title>
            <v-icon>fa-filter</v-icon>
            {{ $t('activity.filter.label') }}
          </v-toolbar-title>
          <v-spacer />
          <v-btn text @click="changeFilterSelection">
            {{ $t('activity.filter.button.confirm') }}
          </v-btn>
        </v-toolbar>
        <v-list>
          <v-list-item
            v-for="streamFilter in streamFilters"
            :key="streamFilter"
            @click="filterToChange = streamFilter.value">
            <v-list-item-title class="align-center d-flex">
              <v-icon v-if="filterToChange === streamFilter.value">fa-check</v-icon>
              <span v-else class="me-6"></span>
              <v-spacer />
              <div>
                {{ streamFilter.text }}
              </div>
              <v-spacer />
              <span class="me-6"></span>
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-sheet>
    </v-bottom-sheet>
  </v-flex>
</template>
<script>
export default {
  data: () => ({
    filterToChange: null,
    bottomMenu: false,
    filter: 'all_stream',
  }),
  computed: {
    streamFilters() {
      return [{
        text: eXo.env.portal.spaceId && this.$t('activity.filter.anyActivity') || this.$t('activity.filter.all'),
        value: 'all_stream',
      },{
        text: this.$t('activity.filter.myActivities'),
        value: 'user_stream',
      },{
        text: this.$t('activity.filter.favoriteActivities'),
        value: 'user_favorite_stream',
      },{
        text: this.$t('activity.filter.manageSpaces'),
        value: 'manage_spaces_stream',
        enable: !eXo.env.portal.spaceId,
      },{
        text: this.$t('activity.filter.favoriteSpaces'),
        value: 'favorite_spaces_stream',
        enable: eXo.env.portal.spaceFavoritesEnabled && !eXo.env.portal.spaceId,
      }].filter(filter => filter.enable == null || filter.enable === true);
    },
  },
  created() {
    this.$root.$on('activity-stream-stored-filter-applied', filter => {
      this.filter = filter;
    });
  },
  methods: {
    applyFilter() {
      document.dispatchEvent(new CustomEvent('activity-stream-type-filter-applied', {detail: this.filter}));
      localStorage.setItem('activity-stream-stored-filter', this.filter);
    },
    openBottomMenuFilter() {
      this.filterToChange = this.filter;
      this.bottomMenu = true;
    },
    changeFilterSelection() {
      this.bottomMenu = false;
      this.filter = this.filterToChange;
      this.applyFilter();
    },
  },
};
</script>