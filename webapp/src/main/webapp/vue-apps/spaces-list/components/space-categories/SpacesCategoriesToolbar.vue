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
    <v-card
      v-if="display"
      class="d-flex align-center mx-5 mt-1"
      min-height="34"
      flat>
      <div v-if="$root.selectedCategoryId" class="d-flex align-center flex-grow-0 flex-shrink-0 max-width-fit overflow-x-auto specific-scrollbar">
        <v-btn
          class="hidden-xs-only"
          height="32"
          width="32"
          icon
          @click="$root.selectedCategoryId = null">
          <v-icon size="24">fa-home</v-icon>
        </v-btn>
        <div
          v-for="(category, index) in breadcrumbToDisplay"
          :key="category.id">
          <div class="d-flex align-center">
            <v-icon
              :class="index === 0 && 'hidden-xs-only'"
              class="mx-2"
              size="16">
              {{ chevronIcon }}
            </v-icon>
            <spaces-category-chip
              :category="category"
              class="flex-shrink-0"
              selected
              @click="selectCategory(category)" />
          </div>
        </div>
        <v-divider
          v-if="selectedSubcategories?.length && level < 2"
          class="mx-4"
          vertical />
      </div>
      <template v-if="spacesSize">
        <spaces-category-chips-group
          v-if="level < 2"
          :categories="selectedSubcategories"
          class="flex-grow-1 flex-shrink-1"
          @select="selectCategory" />
      </template>
    </v-card>
    <spaces-category-tabs-group
      v-if="level > 1"
      :selected-category="selectedSecondLevelCategory"
      @select="selectCategory" />
    <spaces-categories-list-drawer
      ref="drawer" />
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
    depth: 4,
    pageSize: 10,
    refresh: 1,
    chipsWidthPerCategory: 1,
  }),
  computed: {
    display() {
      return this.categories.length > 0;
    },
    breadcrumb() {
      return this.selectedCategory && this.getBreadcrumb(this.selectedCategory);
    },
    level() {
      return this.breadcrumb?.length || 0;
    },
    breadcrumbToDisplay() {
      return this.breadcrumb;
    },
    breadcrumbToDisplaySize() {
      return this.breadcrumbToDisplay?.length || 0;
    },
    selectedSecondLevelCategory() {
      return this.breadcrumb?.[1];
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
    selectCategory(category) {
      if (this.$root.selectedCategoryId === category.id) {
        const categoryIndex = this.breadcrumb.findIndex(c => c.id === category.id);
        this.$root.selectedCategoryId = categoryIndex > 0 ? this.breadcrumb[categoryIndex - 1]?.id : null;
      } else {
        this.$root.selectedCategoryId = category.id;
      }
    },
  },
};
</script>