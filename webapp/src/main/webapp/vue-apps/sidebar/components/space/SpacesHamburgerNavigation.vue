<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <div class="recentDrawer d-flex flex-column full-height full-width overflow-hidden">
    <v-flex v-if="initialized || hasSpaces" class="filterSpaces d-flex align-center flex-grow-0 flex-shrink-0">
      <v-card
        min-height="58"
        class="d-flex align-center full-width transparent"
        flat>
        <v-list-item class="text-truncate full-width" dense>
          <v-list-item-icon
            v-if="!$root.displaySequentially"
            class="backToMenu ms-0 me-2 icon-default-color justify-center"
            @click="$emit('close')">
            <v-icon size="20">{{ $vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left' }}</v-icon>
          </v-list-item-icon>
          <v-list-item-content v-if="!showFilter" class="overflow-hidden">
            <v-list-item-title class="text-truncate text-start font-weight-bold">
              {{ title }}
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-content v-if="showFilter">
            <v-text-field
              v-model="keyword"
              :placeholder="$t('menu.spaces.recentSpaces')"
              :loading="loading"
              class="recentSpacesFilter border-bottom-color pt-0 mt-0"
              single-line
              hide-details
              required
              autofocus />
          </v-list-item-content>
          <v-list-item-action class="d-flex flex-row ms-auto my-auto">
            <space-creation-button
              v-show="!showFilter && canCreateSpace"
              icon
              :icon-size="20"
              left
              set-menu-visibility
              require-form-drawer />
            <v-btn
              v-if="selectedFilterIndex !== 2"
              :title="$t('menu.spaces.filterBySpaceTooltip')"
              class="ms-2"
              icon
              @click="showFilter = !showFilter">
              <v-icon size="20">{{ showFilter && 'fa-times' || 'fa-filter' }}</v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
      </v-card>
    </v-flex>
    <div class="position-relative flex-grow-0 flex-shrink-0">
      <v-progress-linear
        v-if="(!initialized && !hasSpaces) && loading"
        class="position-absolute ful-width"
        indeterminate />
    </div>
    <v-flex v-if="initialized || hasSpaces" class="filterSpaces d-flex align-center flex-grow-0 flex-shrink-0">
      <v-tabs
        v-model="selectedFilterIndex"
        background-color="transparent"
        slider-size="4"
        fixed-tabs>
        <v-tab
          value="recent"
          class="text-header">
          {{ $t('menu.spaces.recent') }}
        </v-tab>
        <v-tab
          value="favorite"
          class="text-header">
          {{ $t('menu.spaces.favorite') }}
        </v-tab>
        <v-tab
          v-if="!$root.openedSpaceTemplateId && !$root.openedSpaceCategoryId"
          value="unread"
          class="text-header">
          {{ $t('menu.spaces.unread') }}
        </v-tab>
      </v-tabs>
      <v-divider />
    </v-flex>
    <spaces-navigation-empty
      v-if="!hasSpaces && !loading"
      :keyword="keyword"
      :filter-type="filterType"
      class="pa-5 flex-grow-0 flex-shrink-0" />
    <spaces-navigation-content
      :limit="itemsToShow"
      :page-size="itemsToShow"
      :keyword="keyword"
      :opened-space="openedSpace"
      :filter-type="filterType"
      show-more-button
      third-level
      class="recentSpacesWrapper overflow-x-hidden overflow-y-auto specific-scrollbar"
      @open-space-panel="$emit('open-space-panel',$event)"
      @loading="loading = $event"
      @spaces-count="hasSpaces = $event" />
    <template v-if="$root.openedSpacesUrl">
      <v-spacer />
      <v-divider />
      <div class="d-flex align-center justify-end my-2 mx-4">
        <v-btn
          :href="$root.openedSpacesUrl"
          color="primary"
          outlined>
          {{ $t('spacesList.label.viewAllSpaces') }}
        </v-btn>
      </div>
    </template>
  </div>
</template>
<script>
export default {
  props: {
    openedSpace: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    itemsToShow: 15,
    showFilter: false,
    selectedFilterIndex: 0,
    loading: false,
    initialized: false,
    hasSpaces: false,
    keyword: '',
  }),
  computed: {
    filterType() {
      if (this.selectedFilterIndex === 0) {
        return 'lastVisited';
      } else if (this.selectedFilterIndex === 1) {
        return 'favorite';
      } else {
        return 'unread';
      }
    },
    title() {
      if (this.$root.openedSpaceTemplateId) {
        return this.$root.openedSpaceTemplateName;
      } else if (this.$root.openedSpaceCategoryId) {
        return this.$root.openedSpaceCategoryName;
      } else {
        return this.$t('menu.spaces.yourSpaces');
      }
    },
    canCreateSpace() {
      return (!this.$root.openedSpaceTemplateId && this.$root.spaceTemplates?.length)
        || (this.$root.openedSpaceTemplateId && this.$root.spaceTemplates?.find(t => Number(t.id) === Number(this.$root.openedSpaceTemplateId)));
    },
  },
  watch: {
    loading() {
      if (!this.loading) {
        this.initialized = true;
      }
    },
  },
  created() {
    this.init();
  },
  methods: {
    async init() {
      if (!this.$root.spaceTemplates) {
        this.$root.spaceTemplates = await this.$spaceTemplateService.getSpaceTemplates();
      }
    },
    closeMenu() {
      this.$emit('close-menu');
    },
    closeFilter() {
      this.keyword = '';
      this.showFilter = false;
    },
    getSpacesPage(item) {
      if (this.itemsToShow <= this.spacesList.length) {
        const l = this.spacesList.length - this.itemsToShow;
        if ( l > item ) {
          this.itemsToShow+=item;
        } else {
          this.itemsToShow+=l;
          this.showButton = false;
        }
      }
    },
    leftNavigationActionEvent(clickedItem) {
      document.dispatchEvent(new CustomEvent('space-left-navigation-action', {detail: clickedItem} ));
    },
    openFilter() {
      this.showFilter = true;
      this.leftNavigationActionEvent('filterBySpaces');
    },
  }
};
</script>
