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
  <div>
    <div v-if="display" class="d-flex flex-wrap mx-5 mt-2">
      <div v-if="$root.selectedCategoryId" class="d-flex align-center">
        <v-btn
          height="32"
          width="32"
          icon
          @click="$root.selectedCategoryId = null">
          <v-icon size="24">fa-home</v-icon>
        </v-btn>
        <div
          v-for="category in breadcrumb"
          :key="category.id"
          class="d-flex align-center">
          <v-icon size="16" class="ms-1 me-2">{{ chevronIcon }}</v-icon>
          <spaces-category-chip
            :category="category"
            class="flex-shrink-0 me-2"
            selected />
        </div>
        <v-divider
          v-if="selectedSubcategories?.length"
          class="mx-4"
          vertical />
      </div>
      <template v-if="spacesSize">
        <spaces-category-chip
          v-for="category in selectedSubcategories"
          :key="category.id"
          :category="category"
          class="flex-shrink-0 me-2" />
      </template>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    spacesSize: {
      type: Number,
      default: () => 0,
    }
  },
  data: () => ({
    categoryTree: null,
    categoryRootId: null,
    loading: false,
    depth: 10,
    pageSize: 10,
    refresh: 1,
  }),
  computed: {
    display() {
      return this.categories.length > 0;
    },
    breadcrumb() {
      return this.selectedCategory && this.getBreadcrumb(this.selectedCategory);
    },
    categoryTreeItems() {
      const categories = this.$root.categoryTree && [this.$root.categoryTree] || [];
      return this.filterTree(JSON.parse(JSON.stringify(categories)));
    },
    chevonIcon() {
      return this.$vuetify.rtl && 'fa-chevron-left' || 'fa-chevron-right';
    },
    selectedSubcategories() {
      return this.selectedCategory?.categories;
    },
    categories() {
      const categories = [];
      if (this.categoryTree) {
        this.addSubcategories(this.categoryTree, categories);
      }
      return categories;
    },
    selectedCategory() {
      return this.categories?.find?.(c => c.id === this.$root.selectedCategoryId) || this.categoryTree;
    },
    chevronIcon() {
      return this.$vuetify.rtl && 'fa-chevron-left' || 'fa-chevron-right';
    },
  },
  created() {
    this.init();
  },
  methods: {
    async init() {
      this.loading = true;
      try {
        this.categoryTree = await this.$categoryService.getCategoryTree({
          depth: this.depth,
          offset: 0,
          limit: -1,
        });
      } finally {
        this.categoryRootId = this.categoryTree?.id;
        this.loading = false;
      }
    },
    addSubcategories(item, result, depth) {
      result.push(item);
      item.depth = depth || 0;
      if (item?.categories) {
        item.categories.forEach(cat => this.addSubcategories(cat, result, item.depth + 1));
      }
    },
    getBreadcrumb(category) {
      if (!category || !category.parentId) {
        return [];
      }
      const breadcrumb = [category];
      while (breadcrumb[0].parentId && breadcrumb[0].parentId !== this.categoryRootId) {
        const parentId = breadcrumb?.[0]?.parentId || category.parentId;
        const parentCategory = this.categories?.find(c => c.id === parentId);
        breadcrumb.unshift(parentCategory);
      }
      return breadcrumb;
    },
  },
};
</script>