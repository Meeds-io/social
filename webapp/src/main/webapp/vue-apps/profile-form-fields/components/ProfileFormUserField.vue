<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

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
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

-->
<template>
  <v-autocomplete
    ref="autocomplete"
    :value="value"
    :items="users"
    :multiple="setting.multiValued"
    :chips="setting.multiValued"
    :loading="loading > 0"
    :search-input.sync="searchTerm"
    :placeholder="placeholder"
    :aria-label="name"
    :aria-required="setting.required ? 'true' : null"
    :name="setting.propertyName"
    :rules="rules"
    :error-messages="error"
    :menu-props="{ offsetY: true, closeOnClick: true, closeOnContentClick: true, contentClass: 'profileFormUserMenu' }"
    :return-object="false"
    class="pt-2"
    item-text="fullName"
    item-value="userName"
    prepend-inner-icon="fas fa-ellipsis-h"
    append-icon=""
    hide-no-data
    hide-selected
    cache-items
    clearable
    outlined
    dense
    @change="select">
    <template #item="{ item }">
      <v-list-item-content class="profileFormUserItem">
        <v-list-item-title>{{ item.fullName || item.userName }}</v-list-item-title>
      </v-list-item-content>
    </template>
    <!-- A chip can be removed only while the autocomplete is: its close
         button is reachable from the keyboard -->
    <template #selection="{ item, parent, disabled }">
      <v-chip
        v-if="setting.multiValued"
        :close="!disabled"
        :disabled="disabled"
        class="profileFormFieldChip"
        @click:close="parent.selectItem(item)">
        {{ item.fullName || item.userName }}
      </v-chip>
      <span v-else class="profileFormFieldSelection text-truncate">{{ item.fullName || item.userName }}</span>
    </template>
  </v-autocomplete>
</template>
<script>
const SEARCH_DELAY = 300;

/**
 * A user attribute: a search on the enabled users, as the groups
 * administration's member picker runs it, chips when multi-valued
 */
export default {
  props: {
    setting: {
      type: Object,
      required: true,
    },
    value: {
      type: [String, Array],
      default: null,
    },
    name: {
      type: String,
      default: null,
    },
    placeholder: {
      type: String,
      default: null,
    },
    error: {
      type: String,
      default: null,
    },
    rules: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    users: [],
    searchTerm: null,
    loading: 0,
    searchTimer: null,
  }),
  watch: {
    // One timer, restarted at each keystroke: the search runs on the last one
    searchTerm(term) {
      window.clearTimeout(this.searchTimer);
      if (term?.trim()) {
        this.searchTimer = window.setTimeout(() => this.searchUsers(term), SEARCH_DELAY);
      }
    },
  },
  beforeDestroy() {
    window.clearTimeout(this.searchTimer);
  },
  methods: {
    searchUsers(term) {
      this.restoreFocus();
      this.loading++;
      return this.$userService.getUsersByStatus(term, 0, 20, 'ENABLED')
        .then(data => {
          if (this.searchTerm === term) {
            this.users = data?.entities || [];
          }
        })
        .finally(() => this.loading--);
    },
    // A pick closes the menu and leaves Vuetify's focused state: the field
    // takes it back before the next search, only while its own input still
    // has the focus, and puts the caret back at the end only when it had
    // lost that state
    restoreFocus() {
      const autocomplete = this.$refs.autocomplete;
      const input = autocomplete?.$refs?.input;
      if (!input || document.activeElement !== input) {
        return;
      }
      const wasFocused = autocomplete.isFocused;
      autocomplete.focus();
      if (!wasFocused) {
        this.$nextTick(() => input.setSelectionRange(input.value.length, input.value.length));
      }
    },
    select(value) {
      this.searchTerm = null;
      this.$emit('input', value);
    },
  },
};
</script>
