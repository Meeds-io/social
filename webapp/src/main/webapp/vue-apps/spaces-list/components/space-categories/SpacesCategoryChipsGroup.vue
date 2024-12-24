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
  <div class="d-flex flex-row overflow-hidden position-relative">
    <template v-if="display">
      <spaces-category-chip
        v-for="(category, index) in filteredCategories"
        :key="category.id"
        :category="category"
        :class="!initialized && 'invisible'"
        class="flex-shrink-0 me-2"
        @initialized="setCategoryChipWidth(index, $event)"
        @click="$root.selectedCategoryId = category.id" />
    </template>
    <v-chip
      ref="moreButton"
      :class="remainingSize < 1 && 'invisible'"
      color="grey"
      dark
      @click="$refs.drawer.open(categories)">
      {{ $t('spacesList.categories.more', {
        0: remainingSize,
      }) }}
    </v-chip>
    <spaces-categories-list-drawer
      ref="drawer" />
  </div>
</template>
<script>
export default {
  props: {
    categories: {
      type: Array,
      default: null,
    }
  },
  data: () => ({
    resizeObserver: null,
    parentWidth: 0,
    moreButtonWidth: 0,
    display: true,
    chipsCount: 0,
    limitCount: 0,
  }),
  computed: {
    filteredCategories() {
      return this.limitCount && this.categories?.slice?.(0, this.limitCount) || this.categories || [];
    },
    categoriesCount() {
      return this.categories?.length || 0;
    },
    remainingSize() {
      return this.categoriesCount - this.limitCount;
    },
    initialized() {
      return this.limitCount > 0 || this.chipsCount === this.categoriesCount;
    },
  },
  watch: {
    chipsCount() {
      if (this.chipsCount === this.categoriesCount && this.limitCount === 0) {
        this.limitCount = this.chipsCount;
      }
    },
    categories() {
      this.refreshWidth();
    },
  },
  mounted() {
    this.resizeObserver = new ResizeObserver(this.refreshWidth).observe(this.$el);
  },
  beforeDestroy() {
    this.resizeObserver?.disconnect?.();
  },
  methods: {
    async refreshWidth() {
      if (this.parentWidth === this.$el.clientWidth - this.moreButtonWidth) {
        return;
      }
      this.setParentWidth();
      this.display = false;
      try {
        await this.$nextTick();
        this.chipsCount = 0;
        this.limitCount = 0;
        await this.$nextTick();
      } finally {
        window.setTimeout(() => {
          this.display = true;
        }, 10);
      }
    },
    setCategoryChipWidth(index, width) {
      if (!this.parentWidth) {
        this.setParentWidth();
      }
      if (width < this.parentWidth) {
        this.chipsCount = Math.max(index + 1, this.chipsCount);
      } else {
        this.limitCount = this.limitCount === 0 ? index : Math.min(this.limitCount, index);
      }
    },
    setParentWidth() {
      if (!this.moreButtonWidth && this.$refs?.moreButton) {
        this.moreButtonWidth = this.$refs.moreButton.$el.offsetWidth + 16;
      }
      this.parentWidth = this.$el.clientWidth - this.moreButtonWidth;
    },
  },
};
</script>