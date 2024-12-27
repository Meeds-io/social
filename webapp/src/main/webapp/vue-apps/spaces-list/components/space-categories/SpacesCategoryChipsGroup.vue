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
  <div class="d-flex align-center specific-scrollbar overflow-x-auto position-relative d-inline text-no-wrap">
    <div v-if="initialized" class="flex-grow-0 flex-shrink-1 overflow-hidden">
      <spaces-category-chip
        v-for="(category, index) in categories"
        :key="category.id"
        :category="category"
        :breadcrumb="index > 1"
        :parent-width="parentWidth"
        chip-class="flex-shrink-0 me-2"
        @initialized="setVisible(category, $event)"
        @select="openCategory" />
    </div>
    <v-chip
      ref="moreButton"
      :class="{
        'invisible' : !hasInvisibleItems,
      }"
      class="flex-shrink-0 flex-grow-0 ms-2"
      color="grey"
      dark
      @click="$root.$emit('spaces-list-category-open', categories)">
      <v-card
        :min-width="85"
        :max-width="85"
        color="transparent"
        class="text-truncate"
        flat>
        {{ $t('spacesList.categories.more', {
          0: remainingSize,
        }) }}
      </v-card>
    </v-chip>
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
    initialized: false,
    invisibleIds: new Set(),
    remainingSize: 0,
  }),
  computed: {
    hasInvisibleItems() {
      return this.remainingSize > 0;
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