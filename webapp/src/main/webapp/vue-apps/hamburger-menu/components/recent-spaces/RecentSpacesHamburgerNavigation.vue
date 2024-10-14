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
  <v-container class="recentDrawer" flat>
    <v-flex v-if="initialized || hasSpaces" class="filterSpaces d-flex align-center">
      <v-list-item-icon
        v-if="!displaySequentially"
        class="backToMenu my-5 mx-2 icon-default-color justify-center"
        @click="$emit('close')">
        <v-icon
          v-if="$root.ltr"
          class="fas fa-arrow-left"
          small />
        <v-icon
          v-else
          class="fas fa-arrow-right"
          small />
      </v-list-item-icon>
      <v-list-item class="recentSpacesTitle px-2">
        <v-list-item-icon 
          class="me-2 align-self-center " 
          @click="closeMenu()"> 
          <v-icon size="20" class="disabled--text">fas fa-filter </v-icon>
        </v-list-item-icon>
        <v-list-item-content v-if="showFilter" class="recentSpacesTitleLabel">
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
        <v-list-item-content
          v-else
          class="recentSpacesTitleLabel pt-1 pb-2px disabled--text border-bottom-color "
          @click="openFilter()">
          {{ $t('menu.spaces.recentSpaces') }}
        </v-list-item-content>
        <v-list-item-action v-if="showFilter" class="recentSpacesTitleIcon position-absolute r-3">
          <v-btn
            text
            icon
            color="blue-grey darken-1"
            size="22"
            @click="closeFilter()">
            <v-icon size="18">mdi-close</v-icon>
          </v-btn>
        </v-list-item-action>
      </v-list-item>
    </v-flex>
    <div class="position-relative">
      <v-progress-linear
        v-if="(!initialized && !hasSpaces) && loading"
        class="position-absolute ful-width"
        indeterminate />
      <spaces-navigation-empty
        v-if="!hasSpaces && !loading"
        :keyword="keyword"
        class="py-5" />
    </div>
    <spaces-navigation-content
      :limit="itemsToShow"
      :page-size="itemsToShow"
      :keyword="keyword"
      :opened-space="openedSpace"
      show-more-button
      third-level
      class="recentSpacesWrapper mt-4"
      @open-space-panel="$emit('open-space-panel',$event)"
      @loading="loading = $event"
      @spaces-count="hasSpaces = $event" />
  </v-container>
</template>
<script>
export default {
  props: {
    openedSpace: {
      type: Object,
      default: null,
    },
    displaySequentially: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    itemsToShow: 15,
    showFilter: false,
    loading: false,
    initialized: false,
    hasSpaces: false,
    keyword: '',
  }),
  watch: {
    loading() {
      if (!this.loading) {
        this.initialized = true;
      }
    },
  },
  methods: {
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
