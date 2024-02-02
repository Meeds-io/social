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
  <div
    v-if="hasValues"
    class="px-5">
    <p
      v-if="showMessage"
      class="text-color subtitle-1">
      {{ $t('complementaryFilter.suggestions.message') }}
    </p>
    <v-chip
      v-for="suggestion in listSuggestions"
      :key="suggestion.value"
      class="mx-1"
      color="primary"
      :outlined="!isSuggestionSelected(suggestion)"
      @click="selectSuggestion(suggestion)">
      <span
        :class="!isSuggestionSelected(suggestion) && 'primary--text'">
        {{ suggestion.value }}
      </span>
    </v-chip>
  </div>
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
    }
  },
  data() {
    return {
      selections: [],
      suggestions: [],
      tempRemovedSuggestions: []
    };
  },
  computed: {
    hasValues() {
      return this.listSuggestions.length > 0;
    },
    listSuggestions() {
      return this.suggestions;
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
      if (this.selections?.length) {
        this.$emit('filter-changed', this.selections);
      }
    }
  },
  created() {
    this.$root.$on('update-filter-suggestions', this.updateFilterSuggestions);
  },
  methods: {
    selectSuggestion(suggestion) {
      const index = this.getSelectionIndex(suggestion);
      if (index === -1) {
        this.selections.push(suggestion);
        this.removeExistingAttributeType(suggestion);
      } else {
        this.selections.splice(index, 1);
        this.$emit('filter-suggestion-unselected', suggestion);
        this.restoreExistingAttributeType(suggestion);
      }
    },
    getSelectionIndex(suggestion) {
      return this.selections.findIndex(existObject => existObject.value === suggestion.value &&
          existObject.key === suggestion.key);
    },
    removeExistingAttributeType(suggestion) {
      this.suggestions.forEach((existObject, index) => {
        if (existObject.key === suggestion.key && existObject.value !== suggestion.value) {
          this.suggestions.splice(index, 1);
          this.tempRemovedSuggestions.push(existObject);
        }
      });
    },
    restoreExistingAttributeType(suggestion) {
      this.tempRemovedSuggestions.forEach((existObject, index) => {
        if (existObject.key === suggestion.key && existObject.value !== suggestion.value) {
          this.tempRemovedSuggestions.splice(index, 1);
          this.suggestions.push(existObject);
        }
      });
      this.suggestions.sort((a, b) => b.count - a.count);
    },
    isSuggestionSelected(suggestion) {
      return this.getSelectionIndex(suggestion) !== -1;
    },
    updateFilterSuggestions() {
      this.getComplementaryFilterSuggestions();
    },
    getComplementaryFilterSuggestions() {
      return this.$complementaryFilterService.getComplementaryFilterSuggestions(this.listObjectIds, this.listAttributes, this.indexAlias, this.minDocCount)
        .then(suggestions => {
          this.suggestions = suggestions.sort((a, b) => b.count - a.count);
        }).finally(() => {
          this.$emit('build-suggestions-terminated', this.suggestions);
        });
    }
  }
};
</script>
