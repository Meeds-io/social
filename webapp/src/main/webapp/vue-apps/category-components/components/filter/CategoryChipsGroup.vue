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
    :class="[(!isMobile || scrollable) && 'specific-scrollbar overflow-x-auto overflow-y-hidden', scrollable && 'category-chips-thin-scrollbar']"
    class="d-flex align-center position-relative d-inline text-no-wrap">
    <component
      v-if="initialized"
      :is="(isMobile || scrollable) ? 'div' : 'card-carousel'"
      :class="scrollable ? 'flex-grow-0 flex-shrink-0' : 'flex-grow-0 flex-shrink-1 overflow-hidden'"
      hide-arrows
      dense>
      <category-chip
        v-for="(category, index) in categories"
        :ref="`category${index}`"
        :key="category.id"
        :category="category"
        :parent-width="parentWidth"
        :selected-id="selectedId"
        chip-class="flex-shrink-0 me-2"
        breadcrumb
        @initialized="setVisible(category, $event)"
        @select="openCategory" />
    </component>
    <v-btn
      v-if="!isMobile && !scrollable"
      ref="moreButton"
      :class="{
        'invisible' : !hasInvisibleItems,
      }"
      :style="moreButtonStyle"
      class="flex-shrink-0 flex-grow-0 px-0"
      color="primary"
      height="30"
      text
      @click="$emit('open-more', categories)">
      {{ $t('categories.seeAll') }}
    </v-btn>
  </div>
</template>
<script>
export default {
  props: {
    categories: {
      type: Array,
      default: null,
    },
    selectedId: {
      type: Number,
      default: () => 0,
    },
    // When true, always lay the chips out in a horizontally scrollable row (the
    // mobile behaviour) instead of the desktop carousel with a "See all" overflow
    // button — useful in narrow containers such as side drawers.
    scrollable: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    resizeObserver: null,
    parentWidth: 0,
    moreButtonWidth: 0,
    initialized: false,
    invisibleIds: new Set(),
    remainingSize: 0,
  }),
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.mobile;
    },
    hasInvisibleItems() {
      return this.remainingSize > 0;
    },
    firstInvisibleElement() {
      if (this.hasInvisibleItems && this.invisibleIds?.size) {
        const firstInvisibleIndex = this.categories.findIndex(cat => this.invisibleIds.has(cat.id));
        return this.$refs?.[`category${firstInvisibleIndex}`]?.[0]?.$refs?.chip?.$el;
      }
      return null;
    },
    firstInvisibleElementX() {
      if (this.firstInvisibleElement) {
        if (this.$vuetify.rtl) {
          return this.firstInvisibleElement.offsetRight;
        } else {
          return this.firstInvisibleElement.offsetLeft;
        }
      }
      return null;
    },
    moreButtonStyle() {
      if (this.firstInvisibleElement) {
        if (this.$vuetify.rtl) {
          return {
            position: 'absolute',
            right: `${this.firstInvisibleElementX}px`,
          };
        } else {
          return {
            position: 'absolute',
            left: `${this.firstInvisibleElementX}px`,
          };
        }
      }
      return null;
    },
  },
  watch: {
    async categories() {
      this.initialized = false;
      await this.$nextTick();
      if (this.categories?.length) {
        this.categories.forEach(c => c.visible = true);
      }
      this.invisibleIds = new Set();
      this.remainingSize = 0;
      this.setParentWidth();
      this.initialized = true;
    },
  },
  mounted() {
    window.setTimeout(() => {
      this.resizeObserver = new ResizeObserver(this.setParentWidth).observe(this.$el);
      this.initialized = true;
    }, 50);
  },
  beforeDestroy() {
    this.resizeObserver?.disconnect?.();
  },
  methods: {
    setParentWidth() {
      if (!this.$el) {
        return;
      }
      if (!this.moreButtonWidth && this.$refs?.moreButton) {
        this.moreButtonWidth = this.$refs.moreButton.$el.offsetWidth + 8;
      }
      this.parentWidth = this.$el.clientWidth - this.moreButtonWidth;
    },
    openCategory(category) {
      this.$emit('select', category);
    },
    setVisible(category, visible) {
      if (visible) {
        this.invisibleIds.delete(category.id);
      } else {
        this.invisibleIds.add(category.id);
      }
      if (this.timeout) {
        window.clearTimeout(this.timeout);
      }
      this.timeout = window.setTimeout(() => this.remainingSize = this.invisibleIds.size, 50);
    },
  },
};
</script>
<style lang="scss">
/* A thin, subtle horizontal scrollbar for the scrollable (mobile-like) chips row,
   used in narrow containers such as side drawers. */
.category-chips-thin-scrollbar {
  scrollbar-width: thin;
  &::-webkit-scrollbar {
    height: 3px;
  }
  &::-webkit-scrollbar-thumb {
    background-color: rgba(0, 0, 0, 0.16);
    border-radius: 3px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
}
</style>