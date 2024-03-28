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
  <v-app>
    <div
      v-if="isLoading"
      class="width-full py-1 d-flex full-height">
      <v-progress-circular
        class="ma-auto"
        color="primary"
        indeterminate />
    </div>
    <div
      v-else-if="hasValues"
      class="px-3 my-auto">
      <p
        v-if="showMessage"
        class="text-color subtitle-1">
        {{ $t('complementaryFilter.suggestions.message') }}
      </p>
      <complementary-filter-item
        v-for="suggestion in listSuggestions"
        :key="suggestion.value"
        :suggestion="suggestion"
        :is-selected="isSuggestionSelected(suggestion)"
        @select-suggestion="selectSuggestion" />
      <v-btn
        v-if="suggestions.length > 3"
        class="ma-auto"
        :class="otherFilterItemsSelected && 'btn-primary elevation-0'
          || 'grey darken-1'"
        :outlined="!otherFilterItemsSelected"
        x-small
        fab
        color="white"
        @click="openListSuggestionsDrawer">
        <span class="text-body-2">
          +{{ suggestions.length - 3 }}
        </span>
      </v-btn>
    </div>
    <complementary-filter-items-drawer
      ref="filterItemsDrawer"
      :suggestions="suggestions"
      :selections="selections"
      @select-suggestion="selectSuggestion"
      @closed="filterDrawerClosed" />
  </v-app>
</template>

<script>

export default {
  props: {
    objectIds: {
      type: Array,
      default: () => []
    },
    attributes: {
      type: Array,
      default: () => []
    },
    indexAlias: {
      type: String,
      default: null
    },
    minDocCount: {
      type: Number,
      default: () => 2
    },
    showMessage: {
      type: Boolean,
      default: () => true
    },
    loadingCallBack: {
      type: Function,
      default: null
    }
  },
  data() {
    return {
      selections: [],
      suggestions: [],
      tempRemovedSuggestions: [],
      otherFilterItemsSelected: false,
      isLoading: false
    };
  },
  computed: {
    hasValues() {
      return this.listSuggestions.length > 0;
    },
    listSuggestions() {
      return this.suggestions.slice(0, 3);
    },
    listObjectIds() {
      return this.objectIds;
    },
    listAttributes() {
      return this.attributes;
    }
  },
  watch: {
    selections() {
      this.$emit('filter-changed', this.selections);
    }
  },
  created() {
    this.$root.$on('filter-reset-selections', this.resetSelections);
    this.$root.$on('update-filter-suggestions', this.updateFilterSuggestions);
  },
  methods: {
    filterDrawerClosed(close) {
      this.$emit('filter-drawer-closed', close);
    },
    resetSelections() {
      this.otherFilterItemsSelected = false;
      this.selections = [];
    },
    openListSuggestionsDrawer() {
      this.$refs.filterItemsDrawer.open();
    },
    checkOnlyInTopThreeSelection() {
      this.otherFilterItemsSelected = false;
      this.selections.some(suggestion => {
        if (this.listSuggestions.findIndex(existObject => existObject.value === suggestion.value &&
            existObject.key === suggestion.key) === -1) {
          this.otherFilterItemsSelected = true;
          return true;
        }
      });
    },
    selectSuggestion(suggestion) {
      const index = this.getSelectionIndex(suggestion);
      if (index === -1) {
        this.selections.push(suggestion);
      } else {
        this.selections.splice(index, 1);
        this.$emit('filter-suggestion-unselected', suggestion);
      }
      this.checkOnlyInTopThreeSelection();
    },
    getSelectionIndex(suggestion) {
      return this.selections.findIndex(existObject => existObject.value === suggestion.value &&
          existObject.key === suggestion.key);
    },
    isSuggestionSelected(suggestion) {
      return this.getSelectionIndex(suggestion) !== -1;
    },
    updateFilterSuggestions() {
      if (this.listObjectIds?.length <= 1) {
        this.suggestions = [...this.selections].sort((a, b) => b.count - a.count);
      } else {
        this.getComplementaryFilterSuggestions();
      }
    },
    getComplementaryFilterSuggestions() {
      this.isLoading = true;
      this.loadingCallBack(true);
      this.suggestions = [];
      return this.$complementaryFilterService.getComplementaryFilterSuggestions(this.listObjectIds, this.listAttributes, this.indexAlias, this.minDocCount)
        .then(suggestions => {
          this.suggestions = suggestions?.sort((a, b) => b.count - a.count);
        }).finally(() => {
          this.$emit('build-suggestions-terminated', this.suggestions);
          this.loadingCallBack(false);
          this.isLoading = false;
        });
    }
  }
};
</script>
