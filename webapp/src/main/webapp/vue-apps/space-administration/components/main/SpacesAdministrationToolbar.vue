<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

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
  <application-toolbar
    id="spacesListToolbar"
    :right-text-filter="{
      minCharacters: 3,
      placeholder: $t('spacesList.label.filterSpaces'),
      tooltip: $t('spacesList.label.filterSpaces'),
      disabled: $root.isBulkProcessing,
    }"
    :right-filter-button="{
      text: $t('social.spaces.administration.manageSpaces.filter'),
      disabled: $root.isBusy,
      displayText: !$root.isMobile,
    }"
    :filters-count="filtersCount"
    :height="toolbarHeight"
    compact
    class="px-1"
    no-text-truncate
    cols-auto
    @filter-text-input-end-typing="$emit('keyword-changed', $event)"
    @filter-button-click="$root.$emit('spaces-administration-filter-drawer-open')"
    @loading="$emit('loading', $event)">
    <template v-if="!$root.isMobile" #left>
      <div
        v-if="showBulkActions"
        ref="bulkActions"
        class="d-flex flex-wrap gap-4 py-2">
        <component
          v-for="extension in $root.bulkExtensions"
          :key="extension.name"
          :is="extension.componentName" />
      </div>
      <v-btn
        v-else
        id="applicationToolbarLeftButton"
        :aria-label="$t('social.spaces.administration.manageSpaces.spaces.add')"
        :class="$root.isMobile && 'px-0'"
        class="btn btn-primary text-truncate"
        @click="$root.$emit('addNewSpace')">
        <v-icon
          size="18">
          fa-plus
        </v-icon>
        <span
          v-if="!$root.isMobile"
          class="text-truncate text-none ms-2">
          {{ $t('social.spaces.administration.manageSpaces.spaces.add') }}
        </span>
      </v-btn>
    </template>
  </application-toolbar>
</template>
<script>
// Height of one row of bulk actions, button and vertical padding included
const TOOLBAR_SINGLE_ROW_HEIGHT = 56;

const TOOLBAR_VERTICAL_PADDING = 16;

export default {
  data: () => ({
    toolbarHeight: null,
    resizeObserver: null,
  }),
  computed: {
    showBulkActions() {
      return !!this.$root.selectedSpaces.length && !this.$root.isMobile;
    },
    filtersCount() {
      return (this.$root.isFilteredByTemplate ? 1 : 0)
        + (this.$root.isFilteredByRegistration ? 1 : 0)
        + (this.$root.isFilteredByVisibility ? 1 : 0);
    },
  },
  mounted() {
    // The bulk actions wrap on narrow screens: the toolbar, whose height is
    // fixed, follows the height of its actions so that their rows keep
    // their spacing
    this.resizeObserver = new ResizeObserver(() => this.computeToolbarHeight());
    this.observeBulkActions();
  },
  watch: {
    showBulkActions() {
      // The bulk actions are rendered by the toolbar's slot: they are
      // observed once rendered
      this.$nextTick(() => this.observeBulkActions());
    },
  },
  beforeDestroy() {
    this.resizeObserver?.disconnect();
  },
  methods: {
    observeBulkActions() {
      if (this.$refs.bulkActions && this.$refs.bulkActions !== this.observedBulkActions) {
        this.observedBulkActions = this.$refs.bulkActions;
        this.resizeObserver?.observe(this.$refs.bulkActions);
      } else if (!this.$refs.bulkActions && this.observedBulkActions) {
        this.resizeObserver?.unobserve(this.observedBulkActions);
        this.observedBulkActions = null;
        this.toolbarHeight = null;
      }
    },
    computeToolbarHeight() {
      const height = this.$refs.bulkActions?.offsetHeight || 0;
      // A single row fits in the toolbar's default height
      this.toolbarHeight = height > TOOLBAR_SINGLE_ROW_HEIGHT ? height + TOOLBAR_VERTICAL_PADDING : null;
    },
  },
};
</script>
