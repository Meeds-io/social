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
  <div
    :class="[hideOnEmpty && !display ? 'd-none' : 'd-flex']"
    class="flex-column justify-center">
    <v-card
      v-if="display"
      :class="isMobile && 'overflow-x-auto specific-scrollbar'"
      class="d-flex align-center transparent"
      min-height="34"
      flat>
      <categories-breadcrumb
        v-if="displayBreadcrumb"
        :breadcrumb="breadcrumb"
        :selected-id="value"
        class="text-start"
        @select="selectCategory" />
      <v-divider
        v-if="displayDivider"
        class="mx-4"
        vertical />
      <category-chips-group
        v-if="displayChipsSelection"
        :categories="selectedSubcategories"
        :selected-id="value"
        class="flex-grow-1 flex-shrink-1 text-start"
        @select="selectCategory"
        @open-more="openMore" />
    </v-card>
    <category-tabs-group
      v-if="selectedCategoryForTabs"
      :selected-category="selectedCategoryForTabs"
      @select="selectCategory" />
    <categories-list-drawer
      ref="moreCategoriesDrawer"
      @select="selectCategory" />
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
    settingName: {
      type: String,
      default: null,
    },
    filterType: {
      type: String,
      default: () => 'category',
    },
    objectType: {
      type: String,
      default: null,
    },
    spaceId: {
      type: String,
      default: null,
    },
    categoryIds: {
      type: Array,
      default: null,
    },
    excludeCategoryIds: {
      type: Array,
      default: null,
    },
    categoryDepth: {
      type: Number,
      default: () => 4,
    },
    hideOnEmpty: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    categoryTree: null,
    initialize: false,
    loading: false,
    pageSize: 10,
    refresh: 1,
    chipsWidthPerCategory: 1,
  }),
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.mobile;
    },
    categories() {
      const categories = [];
      if (this.categoryTree) {
        this.addSubcategories(this.categoryTree, categories);
      }
      return categories;
    },
    displayBreadcrumb() {
      return this.value;
    },
    displayChipsSelection() {
      return this.level < 2 && this.selectedSubcategories?.length;
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
      return this.categories?.find?.(c => c.id === this.value) || this.categoryTree;
    },
    chevronIcon() {
      return this.$vuetify.rtl && 'fa-chevron-left' || 'fa-chevron-right';
    },
  },
  watch: {
    settingName() {
      this.initialize = true; 
    },
    filterType() {
      this.initialize = true; 
    },
    categoryIds() {
      this.initialize = true; 
    },
    categoryDepth() {
      this.initialize = true; 
    },
    initialize() {
      if (this.initialize) {
        this.init();
      }
    },
  },
  created() {
    this.init();
  },
  methods: {
    async init() {
      this.loading = true;
      try {
        if (this.filterType === 'category' && this.categoryIds?.length) {
          const subCategories = (await Promise.all(this.categoryIds.map(id => this.$categoryService.getCategoryTree({
            parentId: id,
            objectType: this.objectType,
            spaceId: this.spaceId,
            depth: this.categoryDepth,
            offset: 0,
            limit: -1,
            token: this.settingName,
          }).catch(() => null)))).filter(c => c);
          this.categoryTree = {
            id: -1,
            parentId: 0,
            ownerId: subCategories?.[0]?.ownerId,
            categories: subCategories || [],
          };
        } else {
          this.categoryTree = await this.$categoryService.getCategoryTree({
            objectType: this.objectType,
            spaceId: this.spaceId,
            depth: this.categoryDepth,
            offset: 0,
            limit: -1,
            token: this.settingName,
          }).catch(() => ({
            id: -1,
            parentId: 0,
            ownerId: 0,
            categories: [],
          }));
        }
      } finally {
        this.loading = false;
        window.setTimeout(() => this.initialize = false, 100);
      }
    },
    addSubcategories(item, result, depth) {
      result.push(item);
      item.depth = depth || 0;
      if (item?.categories) {
        item.categories = item.categories
          .filter(child => !this.excludeCategoryIds?.includes?.(child?.id));
        item.categories.forEach(cat => {
          this.addSubcategories(cat, result, item.depth + 1);
        });
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
    openMore(categories) {
      this.$refs.moreCategoriesDrawer.open(categories);
    },
    selectCategory(category) {
      if (this.value === category?.id) {
        const categoryIndex = this.breadcrumb.findIndex(c => c.id === category.id);
        this.$emit('input', categoryIndex > 0 ? this.breadcrumb[categoryIndex - 1]?.id : null);
      } else {
        this.$emit('input', category?.id);
      }
    },
  },
};
</script>