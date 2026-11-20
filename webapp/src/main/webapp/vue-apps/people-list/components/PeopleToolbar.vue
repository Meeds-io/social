<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2024 Meeds Association contact@meeds.io

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
  <application-toolbar
    class="people-list-application"
    :right-text-filter="{
      minCharacters: 3,
      placeholder: $t('peopleList.label.filterPeople.placeholder'),
      ariaLabel: $t('peopleList.label.filterPeople'),
      tooltip: $t('peopleList.label.filterPeople')
    }"
    :right-filter-button="{
      hide: hideRightFilterButton,
      text: $t('pepole.advanced.filter.button.title'),
      ariaLabel: filterButtonAriaLabel,
    }"
    :right-select-box="{
      hide: compactDisplay || hideFilter,
      selected: filter,
      items: peopleFilters,
    }"
    :compact="compactDisplay"
    :filters-count="advancedFilterCount"
    @filter-text-input-end-typing="keyword = $event"
    @filter-button-click="openPeopleAdvancedFilterDrawer"
    @filter-select-change="filterValue = $event"
    @filter-expand="filterExpand = $event"
    @toggle-select="updateFilter($event)">
    <!-- always passed: the toolbar reads $slots.left, which is not
         reactive, so a slot removed when the count drops to 0 would leave
         the previous count displayed -->
    <template #left>
      <div
        v-if="headerTitle"
        class="people-list-header-title text-header text-truncate">
        {{ headerTitle }}
      </div>
      <div v-else-if="filterMessage" :class="filterMessageClass">
        {{ filterMessage }}
      </div>
    </template>
    <!-- hidden while the filter bar is open, as in the Directory of Spaces -->
    <template v-if="$root.canEdit && !filterExpand" #right>
      <div class="ms-2 people-list-settings-button-container">
        <!-- hidden visually only: the button stays in the tab order, and
             focusing it shows it -->
        <v-btn
          :style="($root.hover || $root.focusWithin) ? '' : 'opacity: 0;'"
          :title="$t('peopleList.settings.drawer.title')"
          :aria-label="$t('peopleList.settings.drawer.title')"
          class="people-list-settings-button"
          height="36"
          width="36"
          icon
          @click="$root.$emit('people-list-settings-open')">
          <v-icon size="20">fa-cog</v-icon>
        </v-btn>
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
    compactDisplay: {
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
    filterValue: null,
    filterExpand: false,
  }),
  created() {
    this.filterValue = this.filter;
    this.$root.$on('advanced-filter-count', (filterCount) => this.advancedFilterCount = filterCount);
    this.$root.$on('reset-advanced-filter-count', () => {
      this.advancedFilterCount = 0;
    });
  },
  computed: {
    // the viewer's own filters only, never the directory's: its state is
    // said, not only coloured
    filterButtonAriaLabel() {
      return this.advancedFilterCount
        ? this.$t('peopleList.label.filterButton.applied', {0: this.advancedFilterCount})
        : this.$t('pepole.advanced.filter.button.title');
    },
    headerTitle() {
      return this.$root.headerTitle;
    },
    peopleFilters() {
      return [{
        text: this.$t('peopleList.label.filter.all'),
        value: 'all',
      },{
        text: this.$t('peopleList.label.filter.connections'),
        value: 'connections',
      }];
    },
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

