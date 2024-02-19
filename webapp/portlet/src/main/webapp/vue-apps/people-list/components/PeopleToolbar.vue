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
  <application-toolbar
    id="peopleListApplication"
    :right-text-filter="{
      minCharacters: 3,
      placeholder: $t('peopleList.label.filterPeople'),
      tooltip: $t('peopleList.label.filterPeople')
    }"
    :right-filter-button="{
      hide: hideRightFilterButton,
      text: $t('pepole.advanced.filter.button.title'),
    }"
    :right-select-box="{
      hide: isMobile || hideFilter,
      selected:'all',
      items: peopleFilters,
    }"
    :compact="compact"
    :filters-count="advancedFilterCount"
    @filter-text-input-end-typing="keyword = $event"
    @filter-button-click="openPeopleAdvancedFilterDrawer"
    @filter-select-change="filterValue = $event"
    @toggle-select="updateFilter($event)">
    <template
      v-if="filterMessage"
      #left>
      <div
        :class="filterMessageClass">
        {{ filterMessage }}
      </div>
    </template>
  </application-toolbar>
</template>

<script>

export default {
  props: {
    filter: {
      type: String,
      default: null,
    },
    hideFilter: {
      type: Boolean,
      default: false
    },
    hideRightFilterButton: {
      type: Boolean,
      default: false
    },
    compact: {
      type: Boolean,
      default: false
    },
    filterMessage: {
      type: String,
      default: null
    },
    filterMessageClass: {
      type: String,
      default: ''
    }
  },
  data: () => ({
    advancedFilterCount: 0,
    keyword: null,
    filterValue: null
  }),
  created() {
    this.filterValue = this.filter;
    this.$root.$on('advanced-filter-count', (filterCount) => this.advancedFilterCount = filterCount);
    this.$root.$on('reset-advanced-filter-count', () => {
      this.advancedFilterCount = 0;
    });
  },
  computed: {
    peopleFilters() {
      return [{
        text: this.$t('peopleList.label.filter.all'),
        value: 'all',
      },{
        text: this.$t('peopleList.label.filter.connections'),
        value: 'connections',
      }];
    },
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    }
  },
  watch: {
    keyword() {
      this.$emit('keyword-changed', this.keyword);
    },
    filter() {
      this.filterValue = this.filter;
    },
    filterValue() {
      this.updateFilter();
    }
  },
  methods: {
    updateFilter() {
      this.$emit('filter-changed', this.filterValue);
    },
    openPeopleAdvancedFilterDrawer() {
      this.$root.$emit('open-people-advanced-filter-drawer');
    },
  }
};
</script>

