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
  <div v-if="categoriesCount" class="d-flex mb-auto me-2 pt-2px d-inline text-no-wrap">
    <v-fade-transition
      v-for="(c, index) in filteredCategories"
      :key="c.id">
      <div v-if="!initialized || !loading || loadedCategories[c.id]">
        <category-chip
          :ref="`category${index}`"
          :category="c"
          chip-class="flex-shrink-0 me-2"
          breadcrumb
          small
          @select="selectCategory" />
      </div>
    </v-fade-transition>
    <v-btn
      v-if="remainingCount > 0"
      ref="moreButton"
      class="flex-shrink-0 flex-grow-0 px-0 text-subtitle-font-size"
      height="24"
      width="24"
      icon
      @click="openMoreDrawer">
      <span class="text-body text-subtitle-font-size">
        {{ $t('categories.remainingCount', {
          0: remainingCount,
        }) }}
      </span>
    </v-btn>
    <categories-list-drawer
      v-if="moreDrawer"
      ref="drawer"
      @select="selectCategory" />
  </div>
</template>
<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    categories: null,
    moreDrawer: false,
    loading: false,
    initialized: false,
    loadedCategories: {},
  }),
  computed: {
    filteredCategories() {
      return this.categories?.slice?.(0, 2) || [];
    },
    categoriesCount() {
      return this.categories?.length || 0;
    },
    remainingCount() {
      return this.categoriesCount - 2;
    },
    hasInvisibleItems() {
      return this.remainingCount > 0;
    },
  },
  created() {
    this.refreshCategories();
    this.$root.$on('activity-refresh-ui', this.handleUpdateCategories);
  },
  beforeDestroy() {
    this.$root.$off('activity-refresh-ui', this.handleUpdateCategories);
  },
  methods: {
    async refreshCategories() {
      this.loading = true;
      try {
        if (this.activity?.categoryIds?.length) {
          const categories = await Promise.all(
            this.activity.categoryIds
              .map(id => this.$categoryService.getCategory(id).catch(() => null))
          );
          this.categories = categories.filter(c => c);
        } else {
          this.categories = [];
        }
      } finally {
        await this.$nextTick();
        this.initialized = true;
        this.loading = false;
        this.categories.forEach(c => this.$set(this.loadedCategories, c.id, true));
      }
    },
    handleUpdateCategories(activityId) {
      if (activityId === this.activity?.id) {
        this.refreshCategories();
      }
    },
    selectCategory(category) {
      if (this.$root.selectedCategoryId === category.id) {
        this.$root.selectedCategoryId = null;
      } else {
        this.$root.selectedCategoryId = category.id;
      }
    },
    async openMoreDrawer() {
      this.moreDrawer = true;
      await this.$nextTick();
      this.$refs?.drawer?.open?.(this.categories);
    },
  },
};
</script>