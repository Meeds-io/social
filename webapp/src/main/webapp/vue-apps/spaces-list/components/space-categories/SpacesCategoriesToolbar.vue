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
  <div class="d-flex flex-column justify-center full-width">
    <v-card
      v-if="display"
      class="d-flex align-center mx-5 mt-1"
      min-height="34"
      flat>
      <spaces-categories-breadcrumb
        v-if="displayBreadcrumb"
        :breadcrumb="breadcrumb"
        class="text-start"
        @select="selectCategory" />
      <v-divider
        v-if="displayDivider"
        class="mx-4"
        vertical />
      <spaces-category-chips-group
        v-if="displayChipsSelection"
        :categories="selectedSubcategories"
        class="flex-grow-1 flex-shrink-1 text-start"
        @select="selectCategory" />
    </v-card>
    <spaces-category-tabs-group
      v-if="selectedCategoryForTabs"
      :selected-category="selectedCategoryForTabs"
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
    loading: false,
    pageSize: 10,
    refresh: 1,
    chipsWidthPerCategory: 1,
  }),
  computed: {
    categories() {
      const categories = [];
      if (this.categoryTree) {
        this.addSubcategories(this.categoryTree, categories);
      }
      return categories;
    },
    displayBreadcrumb() {
      return this.$root.selectedCategoryId;
    },
    displayChipsSelection() {
      return this.level < 2 && this.spacesSize && this.selectedSubcategories?.length;
    },
    displayDivider() {
      return this.displayChipsSelection && this.displayBreadcrumb;
    },
    display() {
      return this.categories.length > 0 && (this.displayChipsSelection || this.displayBreadcrumb);
    },
    breadcrumb() {
      return this.selectedCategory && this.getBreadcrumb(this.selectedCategory);
    },
    level() {
      return this.breadcrumb?.length || 0;
    },
    selectedCategoryForTabs() {
      return this.level > 1 && this.selectedCategory && (this.selectedCategory?.categories?.length ? this.selectedCategory : this.level > 2 && this.getCategory(this.selectedCategory.parentId));
    },
    selectedSubcategories() {
      return this.selectedCategory?.categories;
    },
    selectedCategory() {
      return this.categories?.find?.(c => c.id === this.$root.selectedCategoryId) || this.categoryTree;
    },
    chevronIcon() {
      return this.$vuetify.rtl && 'fa-chevron-left' || 'fa-chevron-right';
    },
  },
  created() {
    this.$root.$on('spaces-list-settings-updated', this.init);
    this.$root.$on('spaces-list-select-category', this.selectCategory);
    this.init();
  },
  beforeDestroy() {
    this.$root.$off('spaces-list-select-category', this.selectCategory);
    this.$root.$off('spaces-list-settings-updated', this.init);
  },
  methods: {
    async init() {
      this.loading = true;
      try {
        if (this.$root.settings.filterType === 'category' && this.$root.settings.categoryIds?.length) {
          const subCategories = await Promise.all(this.$root.settings.categoryIds.map(id => this.$categoryService.getCategoryTree({
            parentId: id,
            depth: this.$root.categoryDepth,
            offset: 0,
            limit: -1,
            token: this.$root.settingName,
          })));
          this.categoryTree = {
            id: -1,
            parentId: 0,
            ownerId: subCategories?.[0]?.ownerId,
            categories: subCategories,
          };
        } else {
          this.categoryTree = await this.$categoryService.getCategoryTree({
            depth: this.$root.categoryDepth,
            offset: 0,
            limit: -1,
            token: this.$root.settingName,
          });
        }
      } finally {
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
      while (breadcrumb[0].parentId && breadcrumb[0].depth > 1) {
        const parentId = breadcrumb?.[0]?.parentId || category.parentId;
        const parentCategory = this.getCategory(parentId);
        breadcrumb.unshift(parentCategory);
      }
      return breadcrumb;
    },
    getCategory(id) {
      return this.categories?.find?.(c => c.id === id);
    },
    selectCategory(category) {
      if (this.$root.selectedCategoryId === category?.id) {
        const categoryIndex = this.breadcrumb.findIndex(c => c.id === category.id);
        this.$root.selectedCategoryId = categoryIndex > 0 ? this.breadcrumb[categoryIndex - 1]?.id : null;
      } else {
        this.$root.selectedCategoryId = category?.id;
      }
    },
  },
};
</script>