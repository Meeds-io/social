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
  <exo-drawer
    ref="complementaryFilterItemsDrawer"
    allow-expand
    right
    @closed="closed">
    <template #title>
      <div>
        <v-btn
          :aria-label="$t('label.close')"
          class="align-start"
          icon
          @click="hideDrawer">
          <v-icon size="20">
            fas fa-arrow-left
          </v-icon>
        </v-btn>
        <span class="text-color ma-auto">
          {{ $t('complementaryFilter.other.suggestions.message') }}
        </span>
      </div>
    </template>
    <template #content>
      <div class="my-2 mx-5">
        <div
          v-for="suggestion in listSuggestions"
          :key="suggestion.value"
          class="my-2">
          <complementary-filter-item
            :is-selected="isSuggestionSelected(suggestion)"
            :suggestion="suggestion"
            @select-suggestion="selectSuggestion" />
        </div>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
  export default {
    props: {
      suggestions: {
        type: Array,
        default: () => [],
      },
      selections: {
        type: Array,
        default: () => [],
      },
    },
    data () {
      return {
        hide: false,
      };
    },
    computed: {
      listSuggestions () {
        return this.suggestions;
      },
    },
    methods: {
      hideDrawer () {
        this.hide = true;
        this.close();
      },
      closed () {
        this.$emit('closed', !this.hide);
      },
      open () {
        this.hide = false;
        this.$refs.complementaryFilterItemsDrawer.open();
      },
      close () {
        this.$refs.complementaryFilterItemsDrawer.close();
      },
      selectSuggestion (suggestion) {
        this.$emit('select-suggestion', suggestion);
      },
      isSuggestionSelected (suggestion) {
        return this.selections.findIndex(existObject => existObject.value === suggestion.value &&
          existObject.key === suggestion.key) !== -1;
      },
    },
  };
</script>
