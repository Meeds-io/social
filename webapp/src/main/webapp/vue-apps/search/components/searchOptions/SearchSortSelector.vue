<!--
This file is part of the Meeds project (https://meeds.io/).

Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <div class="spaceFilter d-flex flex-row">
    <v-menu
      v-model="menu"
      offset-y
      left
      :close-on-content-click="true">
      <template #activator="{ on, attrs }">
        <v-chip
          ref="menuActivator"
          outlined
          :aria-label="$t('search.sort.by.ariaLabel', {0: selectedSortLabel})"
          tabindex="0"
          class="text-body text-header-color mx-1"
          v-bind="attrs"
          v-on="on"
          @keydown.enter.prevent="on.click"
          @keydown.space.prevent="on.click">
          <v-icon
            size="22"
            class="pe-2"
            aria-hidden="true">
            fas fa-sort
          </v-icon>
          <span class="me-2">{{ selectedSortLabel }}</span>
          <i class="fas fa-chevron-down" aria-hidden="true"></i>
        </v-chip>
      </template>

      <v-list
        ref="menuList"
        :aria-label="$t('search.sort.by.ariaLabel', {0: selectedSortLabel})"
        role="menu"
        @keydown.native="onMenuEntriesKeydown">
        <!-- the selected option is announced as checked and shown with a check mark, not by its place alone -->
        <v-list-item
          v-for="option in sortTypes"
          :key="option.value"
          :aria-checked="String(option.value === selectedSort)"
          role="menuitemradio"
          @click="selectSort(option)">
          <v-list-item-title :class="option.value === selectedSort && 'font-weight-bold'">{{ option.label }}</v-list-item-title>
          <v-list-item-icon v-if="option.value === selectedSort" class="my-auto ms-2">
            <v-icon size="14" aria-hidden="true">fas fa-check</v-icon>
          </v-list-item-icon>
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>

<script>
import menuKeyboard from '../../../common/js/MenuKeyboard.js';

export default {
  mixins: [menuKeyboard],
  data() {
    return {
      menu: false,
      selectedSort: '',
      sortDescending: true,
    };
  },
  computed: {
    sortTypes() {
      return [
        { value: '', label: this.$t('search.sort.by.relevancy.option') },
        { value: 'date', label: this.$t('search.sort.by.date.option') },
      ];
    },
    selectedSortLabel() {
      return this.$t(`search.sort.by.${this.selectedSort || 'relevancy'}`);
    }
  },
  watch: {
    menu(opened) {
      if (opened) {
        this.focusFirstMenuEntry();
      }
    },
  },
  methods: {
    closeKeyboardMenu() {
      this.menu = false;
    },
    selectSort(option) {
      this.selectedSort = option.value;
      // the menu closes on the choice: the focus goes back to its button
      this.$nextTick(() => this.$refs.menuActivator?.$el?.focus());
      this.$root.$emit('sort-changed', option, this.sortDescending);
      if (this.selectedSort === 'date') {
        document.dispatchEvent(new CustomEvent('search-sort-by-date-action'));
      }
    },
  },
};
</script>