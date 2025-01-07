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
  <div v-if="categoriesSize" class="my-4 mx-5 position-relative">
    <v-tabs
      v-model="selectedIndex"
      class="position-relative z-index-one"
      color="text-color"
      height="32">
      <v-tab
        :value="0"
        @click="$emit('select', selectedCategory)">
        {{ $t('spacesList.categories.all') }}
      </v-tab>
      <spaces-category-tab
        v-for="item in categories"
        :key="item.id"
        :category="item"
        :selected-category="selectedCategory"
        @click="$emit('select', $event)" />
    </v-tabs>
    <v-divider class="full-width position-absolute b-0" />
  </div>
</template>
<script>
export default {
  props: {
    selectedCategory: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    selectedIndex: 0,
  }),
  computed: {
    categories() {
      return this.selectedCategory?.categories || [];
    },
    categoriesSize() {
      return this.categories?.length || 0;
    },
    selectedCategoryId() {
      return this.$root.selectedCategoryId;
    },
  },
  watch: {
    selectedCategoryId() {
      this.updateSelectedIndex();
    },
    categories() {
      this.updateSelectedIndex();
    },
  },
  mounted() {
    this.updateSelectedIndex();
  },
  methods: {
    async updateSelectedIndex() {
      const index = this.categories.findIndex(cat => cat.id === this.selectedCategoryId || cat?.categories?.find(subCat => subCat.id === this.selectedCategoryId)) + 1;
      if (index > 0) {
        this.selectedIndex = 0;
        await this.$nextTick();
        this.selectedIndex = index;
      } else {
        this.selectedIndex = 0;
      }
    },
  },
};
</script>