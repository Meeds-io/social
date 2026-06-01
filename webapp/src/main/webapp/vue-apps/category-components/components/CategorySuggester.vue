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
  <v-autocomplete
    ref="autocomplete"
    v-model="category"
    :items="filteredCategories"
    :loading="loading"
    :placeholder="$t('categorySuggester.searchCategories')"
    item-text="name"
    item-value="id"
    class="mx-0 mt-0 mb-4 pa-0 elevation-0 no-border"
    return-object
    no-filter
    attach
    hide-no-data
    hide-selected
    hide-details
    outlined
    dense
    @update:search-input="keyword = $event">
    <template #item="{item}">
      <v-card
        color="transparent"
        max-width="350"
        flat>
        <v-list-item :title="item.name" class="pa-0">
          <v-list-item-icon class="me-3">
            <v-card
              color="transparent"
              min-width="20"
              flat>
              <v-icon size="20">{{ item.icon }}</v-icon>
            </v-card>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title class="text-truncate">
              {{ item.name }}
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-card>
    </template>
  </v-autocomplete>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    initialized: false,
    loading: false,
    categories: null,
    category: null,
    keyword: null,
    searchTimeout: null,
    limit: 25,
  }),
  computed: {
    categoryId() {
      return this.category?.id;
    },
    filteredCategories() {
      return this.categories || [];
    },
  },
  watch: {
    keyword() {
      if (this.initialized) {
        this.searchCategories();
      }
    },
    categoryId() {
      if (this.initialized) {
        this.$emit('input', this.categoryId);
      }
    },
    value() {
      if (this.value !== this.categoryId) {
        this.init();
      }
    },
  },
  async created() {
    try {
      await this.init();
    } finally {
      await this.$nextTick();
      window.setTimeout(() => this.initialized = true, 10);
    }
  },
  methods: {
    async init() {
      if (this.value) {
        this.category = await this.$categoryService.getCategory(this.value).catch(() => null);
        if (this.category) {
          this.categories = [this.category];
        } else {
          this.categories = [];
        }
      } else {
        this.category = null;
        this.categories = [];
        await this.$nextTick();
        if (this.$refs?.autocomplete) {
          this.$refs.autocomplete.isFocused = false;
          this.$refs.autocomplete?.blur();
        }
      }
    },
    async searchCategories() {
      if (this.keyword?.trim?.()?.length) {
        this.loading = true;
        try {
          this.categories = await this.$categoryService.findCategories({
            query: this.keyword,
            limit: this.limit,
          });
        } finally {
          this.loading = false;
        }
      } else {
        this.categories = [];
      }
    },
  },
};
</script>
